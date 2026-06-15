'use strict';
const { app, BrowserWindow, ipcMain, shell, dialog, Tray, Menu, nativeImage } = require('electron');
const path    = require('path');
const http    = require('http');
const https   = require('https');
const net     = require('net');
const fs      = require('fs');
const os      = require('os');
const { exec, execFile, spawn } = require('child_process');
const Store   = require('./store');

// ─────────────────────────────────────────────────────────────────────────────
// Globals
// ─────────────────────────────────────────────────────────────────────────────

let win;
let tray;
let autoInjectInterval = null;
let autoInjectedPids   = new Set();
const store = new Store('config');

const REDIRECT_URI   = 'http://localhost:3847/callback';
const DISCORD_SCOPES = 'identify';
const APP_VERSION    = app.getVersion();

// ─────────────────────────────────────────────────────────────────────────────
// Window
// ─────────────────────────────────────────────────────────────────────────────

function createWindow() {
    win = new BrowserWindow({
        width          : 1060,
        height         : 680,
        minWidth       : 820,
        minHeight      : 560,
        resizable      : true,
        frame          : false,
        transparent    : false,
        backgroundColor: '#080810',
        webPreferences : {
            preload         : path.join(__dirname, 'preload.js'),
            contextIsolation: true,
            nodeIntegration : false,
        },
    });

    win.loadFile(path.join(__dirname, 'src', 'index.html'));

    if (process.env.NODE_ENV === 'development') {
        win.webContents.openDevTools({ mode: 'detach' });
    }

    win.webContents.setWindowOpenHandler(() => ({ action: 'deny' }));

    win.on('close', e => {
        if (store.get('minimiseToTray', false) && tray) {
            e.preventDefault();
            win.hide();
        }
    });
}

function createTray() {
    const iconPath = path.join(__dirname, 'assets', 'icon.png');
    let icon;
    try {
        if (fs.existsSync(iconPath)) {
            icon = nativeImage.createFromPath(iconPath);
        } else {
            icon = nativeImage.createEmpty();
        }
    } catch (_) {
        icon = nativeImage.createEmpty();
    }

    tray = new Tray(icon);
    tray.setToolTip('Quark Ghost Client');
    const menu = Menu.buildFromTemplate([
        { label: 'Open Quark',  click: () => { win.show(); win.focus(); } },
        { label: 'Inject Now',  click: () => { win.show(); win.focus(); win.webContents.send('navigate', 'inject'); } },
        { type: 'separator' },
        { label: 'Scan Processes', click: async () => {
            const list = await scanJavaProcesses();
            const mc   = list.filter(p => p.isMinecraft);
            if (mc.length > 0) {
                tray.setToolTip(`Quark — ${mc.length} MC process(es) found`);
            }
        }},
        { type: 'separator' },
        { label: `v${APP_VERSION}`, enabled: false },
        { label: 'Quit', click: () => { app.exit(0); } },
    ]);
    tray.setContextMenu(menu);
    tray.on('double-click', () => { win.show(); win.focus(); });
}

app.whenReady().then(() => {
    createWindow();
    try { createTray(); } catch (_) {}
    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) createWindow();
    });
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') app.quit();
});

app.on('before-quit', () => {
    autoInjectInterval && clearInterval(autoInjectInterval);
});

// ─────────────────────────────────────────────────────────────────────────────
// IPC – Window controls
// ─────────────────────────────────────────────────────────────────────────────

ipcMain.handle('window:minimize', () => win.minimize());
ipcMain.handle('window:close',    () => { win.close(); app.quit(); });
ipcMain.handle('window:maximize', () => { if (win.isMaximized()) win.unmaximize(); else win.maximize(); });
ipcMain.handle('app:version',     () => APP_VERSION);

// ─────────────────────────────────────────────────────────────────────────────
// IPC – Settings
// ─────────────────────────────────────────────────────────────────────────────

ipcMain.handle('settings:get',    (_e, key)      => store.get(key));
ipcMain.handle('settings:set',    (_e, key, val) => { store.set(key, val); return true; });
ipcMain.handle('settings:getAll', ()             => store.store);

// ─────────────────────────────────────────────────────────────────────────────
// IPC – Config backup / restore
// ─────────────────────────────────────────────────────────────────────────────

ipcMain.handle('config:export', async () => {
    const { filePath } = await dialog.showSaveDialog(win, {
        title  : 'Export Quark Config',
        defaultPath: `quark-backup-${Date.now()}.json`,
        filters: [{ name: 'JSON', extensions: ['json'] }],
    });
    if (!filePath) return false;
    fs.writeFileSync(filePath, JSON.stringify(store.store, null, 2), 'utf8');
    return true;
});

ipcMain.handle('config:import', async () => {
    const { filePaths, canceled } = await dialog.showOpenDialog(win, {
        title  : 'Import Quark Config',
        filters: [{ name: 'JSON', extensions: ['json'] }],
        properties: ['openFile'],
    });
    if (canceled || !filePaths[0]) return false;
    try {
        const data = JSON.parse(fs.readFileSync(filePaths[0], 'utf8'));
        for (const [k, v] of Object.entries(data)) store.set(k, v);
        return true;
    } catch (_) { return false; }
});

// ─────────────────────────────────────────────────────────────────────────────
// IPC – Discord OAuth
// ─────────────────────────────────────────────────────────────────────────────

ipcMain.handle('discord:login', async () => {
    const clientId     = store.get('discordClientId',     '');
    const clientSecret = store.get('discordClientSecret', '');
    if (!clientId) throw new Error('NO_CLIENT_ID');

    const authUrl =
        `https://discord.com/api/oauth2/authorize` +
        `?client_id=${clientId}` +
        `&redirect_uri=${encodeURIComponent(REDIRECT_URI)}` +
        `&response_type=code` +
        `&scope=${DISCORD_SCOPES}`;

    return new Promise((resolve, reject) => {
        let server;
        const timeout = setTimeout(() => {
            if (server) server.close();
            reject(new Error('OAuth timed out after 2 minutes'));
        }, 120_000);

        server = http.createServer(async (req, res) => {
            try {
                const url    = new URL(req.url, 'http://localhost:3847');
                const code   = url.searchParams.get('code');
                const errMsg = url.searchParams.get('error_description');
                if (url.pathname !== '/callback') { res.end(); return; }

                if (!code) {
                    res.writeHead(400); res.end(callbackHtml('Error', errMsg || 'No code', '#FF5555'));
                    clearTimeout(timeout); server.close();
                    reject(new Error(errMsg || 'No code')); return;
                }

                const tokenRes = await fetch('https://discord.com/api/oauth2/token', {
                    method : 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body   : new URLSearchParams({ client_id: clientId, client_secret: clientSecret,
                        grant_type: 'authorization_code', code, redirect_uri: REDIRECT_URI }),
                });
                if (!tokenRes.ok) {
                    const body = await tokenRes.text();
                    res.writeHead(500); res.end(callbackHtml('Token error', body, '#FF5555'));
                    clearTimeout(timeout); server.close();
                    reject(new Error('Token exchange failed: ' + body)); return;
                }
                const tokenData = await tokenRes.json();
                const userRes   = await fetch('https://discord.com/api/users/@me', {
                    headers: { Authorization: `Bearer ${tokenData.access_token}` },
                });
                const user = await userRes.json();
                user.avatarUrl = user.avatar
                    ? `https://cdn.discordapp.com/avatars/${user.id}/${user.avatar}.png?size=128`
                    : `https://cdn.discordapp.com/embed/avatars/${Number(user.id) % 5}.png`;

                store.set('user',        user);
                store.set('accessToken', tokenData.access_token);
                res.writeHead(200);
                res.end(callbackHtml('Logged in!', `Welcome, <strong>${user.username}</strong>. You can close this tab.`, '#A855F7'));
                clearTimeout(timeout); server.close();
                resolve(user);
            } catch (err) {
                clearTimeout(timeout); try { server.close(); } catch (_) {}
                reject(err);
            }
        });
        server.on('error', err => { clearTimeout(timeout); reject(err); });
        server.listen(3847, () => shell.openExternal(authUrl));
    });
});

ipcMain.handle('discord:logout', () => {
    store.delete('user'); store.delete('accessToken'); return true;
});

// ─────────────────────────────────────────────────────────────────────────────
// Process scanning – core helper
// ─────────────────────────────────────────────────────────────────────────────

async function scanJavaProcesses() {
    // Try jps first — gives us real Java process data
    const jpsResult = await tryJps();
    if (jpsResult && jpsResult.length > 0) return jpsResult;

    // Fall back to os-specific command
    return await tryNativeScan();
}

function tryJps() {
    return new Promise(resolve => {
        exec('jps -lm', { timeout: 5000 }, (err, stdout) => {
            if (err || !stdout) { resolve(null); return; }
            const results = [];
            for (const line of stdout.split('\n')) {
                const m = line.match(/^(\d+)\s+(\S+)(.*)$/);
                if (!m) continue;
                const pid     = parseInt(m[1], 10);
                const main    = m[2].trim();
                const rest    = m[3].trim();
                if (/Jps$/i.test(main)) continue; // skip jps itself
                const args     = main + ' ' + rest;
                const isMC     = isMinecraftProcess(args);
                const loader   = detectLoaderFromArgs(args);
                const launcher = detectLauncherFromArgs(args);
                const version  = extractMcVersion(rest);
                const memory   = extractMemory(rest);
                results.push({ pid, name: main, args: rest, loader, launcher, version, memory, isMinecraft: isMC });
            }
            resolve(results.length > 0 ? results : null);
        });
    });
}

// wmic was removed from recent Windows 11 builds, so it can no longer be
// relied on as the only Windows process source — fall back to a PowerShell
// CIM query (Get-CimInstance), which is available on all supported versions.
function tryWmicProcesses() {
    return new Promise(resolve => {
        exec('wmic process where "name like \'%java%\'" get processid,commandline /format:csv', { maxBuffer: 8 * 1024 * 1024, timeout: 8000 }, (err, stdout) => {
            if (err || !stdout) { resolve(null); return; }
            const results = [];
            for (const line of stdout.split('\n')) {
                const parts = line.split(',');
                if (parts.length < 2) continue;
                const pid  = parseInt(parts[parts.length - 1], 10);
                if (isNaN(pid)) continue;
                const args = parts.slice(1, -1).join(',');
                results.push({ pid, args });
            }
            resolve(results.length > 0 ? results : null);
        });
    });
}

function tryPowershellProcesses() {
    return new Promise(resolve => {
        const script = "Get-CimInstance Win32_Process -Filter \"Name LIKE '%java%'\" | Select-Object ProcessId,CommandLine | ConvertTo-Json -Compress";
        execFile('powershell.exe', ['-NoProfile', '-NonInteractive', '-Command', script], { maxBuffer: 8 * 1024 * 1024, timeout: 8000 }, (err, stdout) => {
            if (err || !stdout || !stdout.trim()) { resolve(null); return; }
            try {
                let data = JSON.parse(stdout);
                if (!Array.isArray(data)) data = [data];
                const results = data
                    .map(p => ({ pid: parseInt(p.ProcessId, 10), args: p.CommandLine || '' }))
                    .filter(p => !isNaN(p.pid));
                resolve(results.length > 0 ? results : null);
            } catch (_) {
                resolve(null);
            }
        });
    });
}

function tryUnixProcesses() {
    return new Promise(resolve => {
        exec('ps -eo pid,args', { maxBuffer: 8 * 1024 * 1024, timeout: 8000 }, (err, stdout) => {
            if (err || !stdout) { resolve(null); return; }
            const results = [];
            for (const line of stdout.split('\n')) {
                const m = line.trim().match(/^(\d+)\s+(.*)$/);
                if (!m) continue;
                const args = m[2];
                if (!/java/i.test(args)) continue;
                results.push({ pid: parseInt(m[1], 10), args });
            }
            resolve(results.length > 0 ? results : null);
        });
    });
}

async function tryNativeScan() {
    const isWin = process.platform === 'win32';
    let rows;
    if (isWin) {
        rows = await tryWmicProcesses() || await tryPowershellProcesses();
    } else {
        rows = await tryUnixProcesses();
    }
    if (!rows) return [];

    const results = [];
    const seen = new Set();
    for (const { pid, args } of rows) {
        if (isNaN(pid) || seen.has(pid)) continue;
        seen.add(pid);
        const isMC = isMinecraftProcess(args);
        if (!isMC && results.length > 10) continue;
        results.push({
            pid,
            name   : extractMainClass(args) || 'java',
            args,
            loader  : detectLoaderFromArgs(args),
            launcher: detectLauncherFromArgs(args),
            version : extractMcVersion(args),
            memory  : extractMemory(args),
            isMinecraft: isMC,
        });
    }
    return results;
}

// ─────────────────────────────────────────────────────────────────────────────
// IPC – Process Scanner
// ─────────────────────────────────────────────────────────────────────────────

ipcMain.handle('inject:scan', async () => {
    const all = await scanJavaProcesses();
    // Prioritize Minecraft processes but include all Java processes for manual selection
    const mc  = all.filter(p => p.isMinecraft);
    return mc.length > 0 ? mc : all;
});

// ─────────────────────────────────────────────────────────────────────────────
// IPC – Auto-inject
// ─────────────────────────────────────────────────────────────────────────────

ipcMain.handle('inject:autoStart', () => {
    if (autoInjectInterval) return true;
    autoInjectInterval = setInterval(async () => {
        const all = await scanJavaProcesses();
        for (const proc of all.filter(p => p.isMinecraft)) {
            if (!autoInjectedPids.has(proc.pid)) {
                autoInjectedPids.add(proc.pid);
                win.webContents.send('inject:autoDetected', proc);
            }
        }
        // Clean dead PIDs (rough heuristic: if count drops, reset)
        if (all.length === 0) autoInjectedPids.clear();
    }, 4000);
    return true;
});

ipcMain.handle('inject:autoStop', () => {
    if (autoInjectInterval) { clearInterval(autoInjectInterval); autoInjectInterval = null; }
    return true;
});

// ─────────────────────────────────────────────────────────────────────────────
// IPC – Pure JVM Agent Injection
// ─────────────────────────────────────────────────────────────────────────────

ipcMain.handle('inject:run', async (_e, pid) => {
    const agentJar = resolveAgentJar();
    if (!agentJar) throw new Error('Agent JAR not found. Run the build first.');

    const java = await findJava();

    return new Promise((resolve, reject) => {
        const args = ['-cp', agentJar, 'cc.quark.agent.AttachShim', String(pid), agentJar];

        sendLog(`[Quark] Java: ${java}`);
        sendLog(`[Quark] Agent: ${path.basename(agentJar)}`);
        sendLog(`[Quark] Attaching to PID ${pid}…`);

        let proc;
        try {
            proc = spawn(java, args, { timeout: 30000 });
        } catch (spawnErr) {
            sendLog(`[Quark] spawn failed: ${spawnErr.message}`, 'warn');
            tryJattach(pid, agentJar, resolve, reject, spawnErr.message);
            return;
        }

        let stdout = '', stderr = '';

        proc.stdout.on('data', d => {
            const msg = d.toString().trim();
            stdout += msg;
            sendLog(`[Attach] ${msg}`);
        });
        proc.stderr.on('data', d => {
            const msg = d.toString().trim();
            stderr += msg;
            sendLog(`[Attach/err] ${msg}`, 'warn');
        });
        proc.on('close', code => {
            if (code === 0 || stdout.includes('[SUCCESS]')) {
                autoInjectedPids.add(pid);
                resolve({ success: true, pid, method: 'jvm-attach' });
            } else {
                tryJattach(pid, agentJar, resolve, reject, stderr);
            }
        });
        proc.on('error', err => tryJattach(pid, agentJar, resolve, reject, err.message || ''));
    });
});

function sendLog(msg, level = 'info') {
    if (win && !win.isDestroyed()) {
        win.webContents.send('inject:log', { msg, level });
    }
}

function tryJattach(pid, agentJar, resolve, reject, prevErr) {
    const candidates = ['jattach', '/usr/local/bin/jattach', '/usr/bin/jattach',
        path.join(__dirname, 'bin', 'jattach'),
        path.join(__dirname, 'bin', 'jattach.exe')];

    const tryNext = i => {
        if (i >= candidates.length) { fallbackModsInstall(resolve, reject, prevErr); return; }
        const jattach = candidates[i];
        exec(`"${jattach}" ${Number(pid)} load instrument false "${agentJar.replace(/"/g,'')}=quark"`, (err) => {
            if (!err) {
                autoInjectedPids.add(pid);
                sendLog('[jattach] Attach successful');
                resolve({ success: true, pid, method: 'jattach' });
            } else {
                tryNext(i + 1);
            }
        });
    };
    tryNext(0);
}

function fallbackModsInstall(resolve, reject, prevErr) {
    sendLog('[Quark] Pure injection unavailable — staging as Fabric mod (requires restart)', 'warn');
    try {
        // NOTE: the standalone agent JAR (cc.quark.agent.*) is a javaagent, not a
        // Fabric mod — it has no fabric.mod.json and Fabric Loader would ignore it.
        // The mods-folder fallback needs the actual Fabric mod JAR built by Gradle.
        const modJar = resolveModJar();
        if (!modJar) {
            sendLog('[Quark] No Fabric mod JAR found in build/libs. Build it with ./gradlew build first.', 'error');
            reject(new Error('All injection methods failed. Details: ' + prevErr.slice(0, 300)));
            return;
        }
        const modsDir = getModsFolder();
        if (modsDir) {
            if (!fs.existsSync(modsDir)) fs.mkdirSync(modsDir, { recursive: true });
            const dest = path.join(modsDir, path.basename(modJar));
            fs.copyFileSync(modJar, dest);
            sendLog('[Quark] Mod JAR copied to mods folder. Restart Minecraft.', 'warn');
            resolve({ success: true, pid: 0, method: 'mods-install', requiresRestart: true });
            return;
        }
    } catch (e) {
        sendLog('[Quark] Mods install failed: ' + e.message, 'error');
    }
    reject(new Error('All injection methods failed. Details: ' + prevErr.slice(0, 300)));
}

// ─────────────────────────────────────────────────────────────────────────────
// IPC – Java discovery
// ─────────────────────────────────────────────────────────────────────────────

ipcMain.handle('java:list', async () => {
    const results = [];
    const checked = new Set();

    const add = async (javaPath) => {
        if (checked.has(javaPath) || !fs.existsSync(javaPath)) return;
        checked.add(javaPath);
        return new Promise(resolve => {
            exec(`"${javaPath}" -version 2>&1`, { timeout: 3000 }, (err, stdout, stderr) => {
                const out = (stdout || stderr || '').trim();
                const m   = out.match(/version "([^"]+)"/);
                results.push({ path: javaPath, version: m ? m[1] : 'Unknown' });
                resolve();
            });
        });
    };

    const candidates = [];
    if (process.env.JAVA_HOME) {
        const ext = process.platform === 'win32' ? '.exe' : '';
        candidates.push(path.join(process.env.JAVA_HOME, 'bin', `java${ext}`));
    }

    // Common JDK install paths per platform
    if (process.platform === 'win32') {
        const bases = [
            'C:\\Program Files\\Java', 'C:\\Program Files\\Eclipse Adoptium',
            'C:\\Program Files\\Microsoft', 'C:\\Program Files\\BellSoft',
            'C:\\Program Files (x86)\\Java',
        ];
        for (const base of bases) {
            try {
                if (fs.existsSync(base)) {
                    for (const dir of fs.readdirSync(base)) {
                        candidates.push(path.join(base, dir, 'bin', 'java.exe'));
                    }
                }
            } catch (_) {}
        }
        // Minecraft launchers bundle their own JRE
        const mcRuntime = path.join(process.env.APPDATA || '', '.minecraft', 'runtime');
        if (fs.existsSync(mcRuntime)) {
            try {
                for (const ver of fs.readdirSync(mcRuntime)) {
                    const javaExe = path.join(mcRuntime, ver, 'windows', ver, 'bin', 'java.exe');
                    candidates.push(javaExe);
                }
            } catch (_) {}
        }
    } else if (process.platform === 'darwin') {
        const bases = [
            '/Library/Java/JavaVirtualMachines',
            path.join(os.homedir(), 'Library', 'Java', 'JavaVirtualMachines'),
        ];
        for (const base of bases) {
            try {
                if (fs.existsSync(base)) {
                    for (const dir of fs.readdirSync(base)) {
                        candidates.push(path.join(base, dir, 'Contents', 'Home', 'bin', 'java'));
                    }
                }
            } catch (_) {}
        }
    } else {
        // Linux
        const bases = ['/usr/lib/jvm', '/usr/local/lib/jvm', '/opt/java', '/opt/jdk'];
        for (const base of bases) {
            try {
                if (fs.existsSync(base)) {
                    for (const dir of fs.readdirSync(base)) {
                        candidates.push(path.join(base, dir, 'bin', 'java'));
                    }
                }
            } catch (_) {}
        }
        // Minecraft bundled runtime on Linux
        const mcRuntime = path.join(os.homedir(), '.minecraft', 'runtime');
        if (fs.existsSync(mcRuntime)) {
            try {
                for (const ver of fs.readdirSync(mcRuntime)) {
                    const javaExe = path.join(mcRuntime, ver, 'linux', ver, 'bin', 'java');
                    candidates.push(javaExe);
                }
            } catch (_) {}
        }
    }

    // Also check PATH
    candidates.push('java');

    await Promise.all(candidates.map(c => add(c)));
    return results;
});

// ─────────────────────────────────────────────────────────────────────────────
// IPC – Server ping (Minecraft status protocol)
// ─────────────────────────────────────────────────────────────────────────────

ipcMain.handle('server:ping', (_e, host, port = 25565) => {
    return new Promise(resolve => {
        const start  = Date.now();
        const socket = new net.Socket();
        let   data   = Buffer.alloc(0);
        let   done   = false;

        const finish = result => {
            if (!done) { done = true; socket.destroy(); resolve(result); }
        };

        socket.setTimeout(6000);
        socket.on('timeout', () => finish({ online: false, latency: null, error: 'Timeout' }));
        socket.on('error',   e  => finish({ online: false, latency: null, error: e.message }));

        socket.connect(port, host, () => {
            // Handshake: protocol 767 (1.21.1)
            const hostBuf    = Buffer.from(host, 'utf8');
            const handshake  = buildPacket(0x00,
                writeVarInt(767), writeString(host), writeUShort(port), writeVarInt(1));
            const statusReq  = buildPacket(0x00);
            socket.write(Buffer.concat([handshake, statusReq]));
        });

        socket.on('data', chunk => {
            data = Buffer.concat([data, chunk]);
            try {
                let offset = 0;
                const [pktLen, lb] = readVarInt(data, offset); offset += lb;
                if (data.length < lb + pktLen) return;
                const [pktId,  ib] = readVarInt(data, offset); offset += ib;
                if (pktId !== 0x00) return;
                const [sLen,   sl] = readVarInt(data, offset); offset += sl;
                const json = data.slice(offset, offset + sLen).toString('utf8');
                const s    = JSON.parse(json);
                const latency = Date.now() - start;
                finish({
                    online     : true,
                    latency,
                    players    : { online: s.players?.online ?? 0, max: s.players?.max ?? 0 },
                    version    : s.version?.name ?? 'Unknown',
                    description: extractMotd(s.description),
                    favicon    : s.favicon ?? null,
                });
            } catch (_) {}
        });
    });
});

// VarInt / packet helpers
function writeVarInt(val) {
    const out = [];
    do { let b = val & 0x7F; val >>>= 7; if (val) b |= 0x80; out.push(b); } while (val);
    return Buffer.from(out);
}
function readVarInt(buf, off) {
    let res = 0, shift = 0, n = 0;
    do { const b = buf[off + n++]; res |= (b & 0x7F) << shift; shift += 7; if (!(b & 0x80)) break; } while (shift < 35);
    return [res, n];
}
function writeString(s) { const b = Buffer.from(s, 'utf8'); return Buffer.concat([writeVarInt(b.length), b]); }
function writeUShort(n) { const b = Buffer.alloc(2); b.writeUInt16BE(n, 0); return b; }
function buildPacket(id, ...parts) {
    const body = Buffer.concat([writeVarInt(id), ...parts.filter(Boolean)]);
    return Buffer.concat([writeVarInt(body.length), body]);
}
function extractMotd(desc) {
    if (!desc) return '';
    if (typeof desc === 'string') return desc;
    if (desc.text) return desc.text + (desc.extra || []).map(e => e.text || '').join('');
    return JSON.stringify(desc);
}

// ─────────────────────────────────────────────────────────────────────────────
// IPC – System Info
// ─────────────────────────────────────────────────────────────────────────────

ipcMain.handle('system:info', () => ({
    platform : process.platform,
    arch     : process.arch,
    memory   : Math.round(os.totalmem()  / 1024 / 1024 / 1024),
    freemem  : Math.round(os.freemem()   / 1024 / 1024 / 1024),
    cpus     : os.cpus().length,
    cpuModel : os.cpus()[0]?.model || 'Unknown',
    hostname : os.hostname(),
    uptime   : Math.round(os.uptime() / 3600),
    node     : process.version,
    electron : process.versions.electron,
    appVersion: APP_VERSION,
}));

ipcMain.handle('system:openFolder', (_e, folderPath) => {
    if (folderPath && fs.existsSync(folderPath)) shell.openPath(folderPath);
});

ipcMain.handle('system:selectFile', async () => {
    const result = await dialog.showOpenDialog(win, {
        filters   : [{ name: 'JAR Files', extensions: ['jar'] }],
        properties: ['openFile'],
    });
    return result.canceled ? null : result.filePaths[0];
});

const IMAGE_MIME_TYPES = { png: 'image/png', jpg: 'image/jpeg', jpeg: 'image/jpeg', gif: 'image/gif', webp: 'image/webp', bmp: 'image/bmp' };
const MAX_AVATAR_BYTES = 4 * 1024 * 1024; // 4MB

ipcMain.handle('system:selectImage', async () => {
    const result = await dialog.showOpenDialog(win, {
        title     : 'Choose Profile Picture',
        filters   : [{ name: 'Images', extensions: Object.keys(IMAGE_MIME_TYPES) }],
        properties: ['openFile'],
    });
    if (result.canceled || !result.filePaths[0]) return null;

    const filePath = result.filePaths[0];
    const ext = path.extname(filePath).slice(1).toLowerCase();
    const mime = IMAGE_MIME_TYPES[ext];
    if (!mime) throw new Error('Unsupported image format: ' + ext);

    const stat = fs.statSync(filePath);
    if (stat.size > MAX_AVATAR_BYTES) throw new Error('Image too large (max 4MB)');

    const data = fs.readFileSync(filePath);
    return `data:${mime};base64,${data.toString('base64')}`;
});

ipcMain.handle('system:openExternal', (_e, url) => {
    // Only allow safe URLs
    if (url && (url.startsWith('https://') || url.startsWith('http://localhost'))) {
        shell.openExternal(url);
    }
});

ipcMain.handle('system:gameDirs', () => {
    const dirs = [];
    const home = os.homedir();
    const add  = (label, p) => { if (fs.existsSync(p)) dirs.push({ label, path: p }); };

    if (process.platform === 'win32') {
        const appdata = process.env.APPDATA || '';
        add('Official Launcher',   path.join(appdata, '.minecraft'));
        add('Prism Launcher',      path.join(appdata, 'PrismLauncher'));
        add('MultiMC',             path.join(appdata, 'MultiMC'));
        add('GDLauncher',          path.join(appdata, 'gdlauncher_next'));
        add('CurseForge',          path.join(process.env.LOCALAPPDATA || '', 'CurseForge'));
        add('ATLauncher',          path.join(home,    'Documents', 'ATLauncher'));
        add('Technic Launcher',    path.join(appdata, '.technic'));
        add('SKLauncher',          path.join(appdata, 'SKlauncher'));
        add('TLauncher',           path.join(appdata, '.tlauncher'));
    } else if (process.platform === 'darwin') {
        const libapp = path.join(home, 'Library', 'Application Support');
        add('Official Launcher', path.join(libapp, 'minecraft'));
        add('Prism Launcher',    path.join(libapp, 'PrismLauncher'));
        add('MultiMC',           path.join(home,    'Library', 'Application Support', 'MultiMC'));
        add('GDLauncher',        path.join(libapp, 'gdlauncher_next'));
        add('ATLauncher',        path.join(libapp, 'ATLauncher'));
        add('TLauncher',         path.join(libapp, '.tlauncher'));
    } else {
        const localShare = path.join(home, '.local', 'share');
        add('Official Launcher', path.join(home, '.minecraft'));
        add('Prism Launcher',    path.join(localShare, 'PrismLauncher'));
        add('MultiMC',           path.join(home, '.local', 'share', 'multimc'));
        add('GDLauncher',        path.join(localShare, 'gdlauncher_next'));
        add('ATLauncher',        path.join(home, 'ATLauncher'));
        add('Technic Launcher',  path.join(home, '.technic'));
        add('TLauncher',         path.join(home, '.tlauncher'));
    }
    return dirs;
});

// ─────────────────────────────────────────────────────────────────────────────
// Helpers – Process analysis
// ─────────────────────────────────────────────────────────────────────────────

function isMinecraftProcess(args) {
    if (!args) return false;
    return /minecraft|net\.minecraft|com\.mojang|fabric-loader|forge|neoforge|lunar|badlion|feather|tlauncher|prismlauncher|multimc|gdlauncher|curseforge|atlauncher|technic|sklauncher/i.test(args);
}

function detectLoaderFromArgs(args) {
    if (!args) return 'Vanilla';
    const a = args.toLowerCase();
    if (/lunar/i.test(a))    return 'Lunar';
    if (/badlion/i.test(a))  return 'Badlion';
    if (/feather/i.test(a))  return 'Feather';
    if (/neoforge/i.test(a)) return 'NeoForge';
    if (/forge/i.test(a))    return 'Forge';
    if (/fabric/i.test(a))   return 'Fabric';
    if (/quilt/i.test(a))    return 'Quilt';
    return 'Vanilla';
}

function detectLauncherFromArgs(args) {
    if (!args) return 'Unknown';
    const a = args.toLowerCase();
    if (/lunar/i.test(a))        return 'Lunar Client';
    if (/badlion/i.test(a))      return 'Badlion';
    if (/feather/i.test(a))      return 'Feather';
    if (/prismlauncher/i.test(a))return 'Prism';
    if (/multimc/i.test(a))      return 'MultiMC';
    if (/gdlauncher/i.test(a))   return 'GDLauncher';
    if (/curseforge/i.test(a))   return 'CurseForge';
    if (/atlauncher/i.test(a))   return 'ATLauncher';
    if (/technic/i.test(a))      return 'Technic';
    if (/sklauncher/i.test(a))   return 'SKLauncher';
    if (/tlauncher/i.test(a))    return 'TLauncher';
    if (/com\.mojang/i.test(a))  return 'Official';
    if (/minecraft/i.test(a))    return 'Official';
    return 'Unknown';
}

function extractMcVersion(args) {
    if (!args) return null;
    const m = args.match(/--version\s+([^\s]+)/);
    return m ? m[1] : null;
}

function extractMemory(args) {
    if (!args) return null;
    const m = args.match(/-Xmx(\d+[gGmM])/);
    return m ? m[1].toUpperCase() : null;
}

function extractMainClass(args) {
    if (!args) return null;
    // Look for net.minecraft or fabric main classes
    const m = args.match(/(net\.minecraft\S+|com\.mojang\S+|net\.fabricmc\S+|cpw\.mods\S+|org\.quiltmc\S+)/);
    return m ? m[1] : null;
}

function resolveAgentJar() {
    const custom = store.get('agentPath', '');
    if (custom && fs.existsSync(custom)) return custom;

    // Must be the standalone javaagent JAR (Agent-Class/Premain-Class manifest) —
    // NOT the Fabric mod JAR or the CLI injector JAR, both of which Gradle also
    // drops into build/libs and would make Instrumentation.loadAgent() fail.
    const candidates = [
        path.join(__dirname, 'agent', 'quark-agent.jar'),
        path.join(__dirname, '..', 'build', 'libs', 'quark-agent.jar'),
        path.join(__dirname, 'quark-agent.jar'),
    ];
    return candidates.find(p => fs.existsSync(p)) || null;
}

function resolveModJar() {
    const buildDir = path.join(__dirname, '..', 'build', 'libs');
    if (!fs.existsSync(buildDir)) return null;

    const skip = new Set(['quark-agent.jar', 'quark-injector.jar']);
    const modJars = fs.readdirSync(buildDir)
        .filter(f => /^quark.*\.jar$/i.test(f) && !skip.has(f) && !/-(sources|dev)\.jar$/i.test(f))
        .sort()
        .map(f => path.join(buildDir, f));
    return modJars[0] || null;
}

async function findJava() {
    const customJava = store.get('javaPath', '');
    if (customJava && fs.existsSync(customJava)) return customJava;

    if (process.env.JAVA_HOME) {
        const ext = process.platform === 'win32' ? '.exe' : '';
        const j = path.join(process.env.JAVA_HOME, 'bin', `java${ext}`);
        if (fs.existsSync(j)) return j;
    }
    return 'java';
}

function getModsFolder() {
    const platform = process.platform;
    if (platform === 'win32') {
        const appdata = process.env.APPDATA;
        return appdata ? path.join(appdata, '.minecraft', 'mods') : null;
    } else if (platform === 'darwin') {
        return path.join(os.homedir(), 'Library', 'Application Support', 'minecraft', 'mods');
    } else {
        return path.join(os.homedir(), '.minecraft', 'mods');
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers – HTML callback page
// ─────────────────────────────────────────────────────────────────────────────

function callbackHtml(heading, body, color) {
    return `<!DOCTYPE html><html><head><meta charset="UTF-8">
<style>
  *{margin:0;padding:0;box-sizing:border-box}
  body{background:#080810;color:#fff;font-family:"Segoe UI",sans-serif;
       display:flex;align-items:center;justify-content:center;
       height:100vh;flex-direction:column;gap:12px}
  h2{color:${color};font-size:22px}
  p{color:#888;font-size:14px}
  strong{color:#fff}
</style></head><body>
<h2>${heading}</h2><p>${body}</p>
</body></html>`;
}
