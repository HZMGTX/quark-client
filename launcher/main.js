'use strict';
const { app, BrowserWindow, ipcMain, shell, dialog } = require('electron');
const path    = require('path');
const http    = require('http');
const https   = require('https');
const fs      = require('fs');
const os      = require('os');
const { exec, execFile, spawn } = require('child_process');
const Store   = require('./store');

// ─────────────────────────────────────────────────────────────────────────────
// Globals
// ─────────────────────────────────────────────────────────────────────────────

let win;
const store = new Store('config');

const REDIRECT_URI   = 'http://localhost:3847/callback';
const DISCORD_SCOPES = 'identify';

// ─────────────────────────────────────────────────────────────────────────────
// Window
// ─────────────────────────────────────────────────────────────────────────────

function createWindow() {
    win = new BrowserWindow({
        width          : 920,
        height         : 600,
        minWidth       : 750,
        minHeight      : 520,
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

    // Prevent external navigation
    win.webContents.setWindowOpenHandler(() => ({ action: 'deny' }));
}

app.whenReady().then(() => {
    createWindow();
    app.on('activate', () => { if (BrowserWindow.getAllWindows().length === 0) createWindow(); });
});
app.on('window-all-closed', () => { if (process.platform !== 'darwin') app.quit(); });

// ─────────────────────────────────────────────────────────────────────────────
// IPC – Window controls
// ─────────────────────────────────────────────────────────────────────────────

ipcMain.handle('window:minimize', () => win.minimize());
ipcMain.handle('window:close',    () => { win.close(); app.quit(); });
ipcMain.handle('window:maximize', () => { if (win.isMaximized()) win.unmaximize(); else win.maximize(); });
ipcMain.handle('app:version',     () => app.getVersion());

// ─────────────────────────────────────────────────────────────────────────────
// IPC – Settings
// ─────────────────────────────────────────────────────────────────────────────

ipcMain.handle('settings:get',    (_e, key)      => store.get(key));
ipcMain.handle('settings:set',    (_e, key, val) => { store.set(key, val); return true; });
ipcMain.handle('settings:getAll', ()             => store.store);

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
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ client_id: clientId, client_secret: clientSecret,
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
                res.writeHead(200); res.end(callbackHtml('Logged in!', `Welcome, <strong>${user.username}</strong>. You can close this tab.`, '#A855F7'));
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
// IPC – Process Scanner
// ─────────────────────────────────────────────────────────────────────────────

ipcMain.handle('inject:scan', () => {
    return new Promise(resolve => {
        const isWin = process.platform === 'win32';
        const isMac = process.platform === 'darwin';

        let cmd;
        if (isWin) {
            cmd = 'wmic process where "name like \'%java%\'" get processid,name,commandline /format:csv';
        } else if (isMac) {
            cmd = 'ps -eo pid,comm,args | grep -i java | grep -v grep';
        } else {
            cmd = 'ps -eo pid,comm,args | grep -i java | grep -v grep';
        }

        exec(cmd, { maxBuffer: 4 * 1024 * 1024 }, (err, stdout) => {
            if (err && !stdout) { resolve([]); return; }
            const results = [];

            if (isWin) {
                for (const line of stdout.split('\n')) {
                    const parts = line.split(',');
                    if (parts.length < 3) continue;
                    const pid  = parseInt(parts[parts.length - 1], 10);
                    const name = parts[1]?.replace(/"/g, '').trim() || '';
                    const args = parts.slice(2, -1).join(',') || '';
                    if (/javaw?\.exe/i.test(name) && isMinecraftProcess(args)) {
                        results.push({ pid, name, loader: detectLoaderFromArgs(args) });
                    }
                }
            } else {
                for (const line of stdout.split('\n')) {
                    const parts = line.trim().split(/\s+/);
                    if (parts.length < 2) continue;
                    const pid  = parseInt(parts[0], 10);
                    const name = parts[1] || '';
                    const args = parts.slice(2).join(' ');
                    if (/^java$/i.test(name) && (isMinecraftProcess(args) || results.length === 0)) {
                        results.push({ pid, name: args || 'java', loader: detectLoaderFromArgs(args) });
                    }
                }
            }

            // De-duplicate by PID
            const seen = new Set();
            resolve(results.filter(r => { if (seen.has(r.pid) || isNaN(r.pid)) return false; seen.add(r.pid); return true; }));
        });
    });
});

function isMinecraftProcess(args) {
    if (!args) return false;
    return /minecraft|net\.minecraft|com\.mojang|fabric|forge|neoforge|lunar|badlion/i.test(args);
}

function detectLoaderFromArgs(args) {
    if (!args) return 'Vanilla';
    if (/lunar/i.test(args))   return 'Lunar';
    if (/badlion/i.test(args)) return 'Badlion';
    if (/neoforge/i.test(args)) return 'NeoForge';
    if (/forge/i.test(args))   return 'Forge';
    if (/fabric/i.test(args))  return 'Fabric';
    return 'Vanilla';
}

// ─────────────────────────────────────────────────────────────────────────────
// IPC – Pure JVM Agent Injection (no JAR in mods folder)
// ─────────────────────────────────────────────────────────────────────────────

ipcMain.handle('inject:run', async (_e, pid) => {
    console.log(`[Quark Inject] Attaching to PID ${pid}`);

    // 1. Locate the agent JAR
    const agentJar = resolveAgentJar();
    if (!agentJar) throw new Error('Agent JAR not found. Build the project first (gradle shadowJar).');

    // 2. Locate a Java executable capable of running the attach shim
    const java = await findJava();

    // 3. Run the JVM Attach shim — this attaches to the PID and loads our agent
    // The shim class is bundled inside the agent JAR itself
    return new Promise((resolve, reject) => {
        const attachShimClass = 'cc.quark.agent.AttachShim';
        const args = [
            '-cp', agentJar,
            attachShimClass,
            String(pid),
            agentJar,
        ];

        console.log(`[Quark Inject] Running: ${java} ${args.join(' ')}`);

        const proc = spawn(java, args, { timeout: 30000 });
        let stdout = '', stderr = '';
        proc.stdout.on('data', d => { stdout += d; console.log('[Attach]', d.toString().trim()); });
        proc.stderr.on('data', d => { stderr += d; console.error('[Attach]', d.toString().trim()); });
        proc.on('close', code => {
            if (code === 0 || stdout.includes('[SUCCESS]')) {
                resolve({ success: true, pid, method: 'jvm-attach' });
            } else {
                // Fallback: try jattach if available
                tryJattach(pid, agentJar, resolve, reject, stderr);
            }
        });
        proc.on('error', () => tryJattach(pid, agentJar, resolve, reject, ''));
    });
});

function tryJattach(pid, agentJar, resolve, reject, prevErr) {
    // jattach is a lightweight C tool for JVM attach on all platforms
    // Try common install locations
    const candidates = ['jattach', '/usr/local/bin/jattach', '/usr/bin/jattach'];
    const tryNext = (i) => {
        if (i >= candidates.length) {
            // Final fallback: copy to mods folder (legacy mode)
            fallbackModsInstall(agentJar, resolve, reject, prevErr);
            return;
        }
        const jattach = candidates[i];
        exec(`"${jattach}" ${pid} load instrument false "${agentJar}=quark"`, (err, out, errOut) => {
            if (!err) {
                resolve({ success: true, pid, method: 'jattach' });
            } else {
                tryNext(i + 1);
            }
        });
    };
    tryNext(0);
}

function fallbackModsInstall(agentJar, resolve, reject, prevErr) {
    // If pure injection fails, install as a Fabric mod for next launch
    console.warn('[Quark Inject] Pure injection unavailable — staging as Fabric mod (requires restart)');
    try {
        const modsDir = getModsFolder();
        if (modsDir) {
            if (!fs.existsSync(modsDir)) fs.mkdirSync(modsDir, { recursive: true });
            const dest = path.join(modsDir, path.basename(agentJar));
            fs.copyFileSync(agentJar, dest);
            resolve({ success: true, pid: 0, method: 'mods-install', requiresRestart: true });
            return;
        }
    } catch (e) {
        console.error('[Quark Inject] Mods install failed:', e);
    }
    reject(new Error('Injection failed. No attach method succeeded. Details: ' + prevErr.slice(0, 200)));
}

function resolveAgentJar() {
    // Check custom path first
    const custom = store.get('agentPath', '');
    if (custom && fs.existsSync(custom)) return custom;

    // Standard build output locations
    const candidates = [
        path.join(__dirname, '..', 'build', 'libs', 'quark-agent.jar'),
        path.join(__dirname, '..', 'build', 'libs', 'quark-1.21.1-1.0.0+mc1.21.1.jar'),
        path.join(__dirname, '..', 'versions', '1.21.1', 'build', 'libs', 'quark-1.21.1-1.0.0+mc1.21.1.jar'),
        path.join(__dirname, 'agent.jar'),
    ];
    // Also glob for any quark*.jar in build
    const buildDir = path.join(__dirname, '..', 'build', 'libs');
    if (fs.existsSync(buildDir)) {
        for (const f of fs.readdirSync(buildDir)) {
            if (/quark.*\.jar$/i.test(f)) candidates.unshift(path.join(buildDir, f));
        }
    }
    return candidates.find(p => fs.existsSync(p)) || null;
}

async function findJava() {
    // 1. JAVA_HOME
    if (process.env.JAVA_HOME) {
        const j = path.join(process.env.JAVA_HOME, 'bin', process.platform === 'win32' ? 'java.exe' : 'java');
        if (fs.existsSync(j)) return j;
    }
    // 2. Try 'java' on PATH
    return 'java';
}

function getModsFolder() {
    const platform = process.platform;
    let base;
    if (platform === 'win32') {
        base = process.env.APPDATA;
    } else if (platform === 'darwin') {
        base = path.join(os.homedir(), 'Library', 'Application Support');
    } else {
        base = path.join(os.homedir(), '.local', 'share');
    }
    return base ? path.join(base, '.minecraft', 'mods') : null;
}

// ─────────────────────────────────────────────────────────────────────────────
// IPC – System Info
// ─────────────────────────────────────────────────────────────────────────────

ipcMain.handle('system:info', () => ({
    platform: process.platform,
    arch:     process.arch,
    memory:   Math.round(os.totalmem() / 1024 / 1024 / 1024),
    cpus:     os.cpus().length,
    hostname: os.hostname(),
    node:     process.version,
    electron: process.versions.electron,
}));

ipcMain.handle('system:openFolder', (_e, folderPath) => {
    if (folderPath && fs.existsSync(folderPath)) shell.openPath(folderPath);
});

ipcMain.handle('system:selectFile', async () => {
    const result = await dialog.showOpenDialog(win, {
        filters: [{ name: 'JAR Files', extensions: ['jar'] }],
        properties: ['openFile'],
    });
    return result.canceled ? null : result.filePaths[0];
});

// ─────────────────────────────────────────────────────────────────────────────
// IPC – Global Chat relay
// ─────────────────────────────────────────────────────────────────────────────

let chatServerProcess = null;

ipcMain.handle('chat:serverStart', (_e, port) => {
    if (chatServerProcess) return { running: true, port };
    const serverScript = path.join(__dirname, '..', 'server', 'chat-server.js');
    if (!fs.existsSync(serverScript)) throw new Error('Chat server not found.');
    const p = parseInt(port, 10) || 8765;
    chatServerProcess = exec(`node "${serverScript}"`, { env: { ...process.env, PORT: p } });
    chatServerProcess.on('exit', () => { chatServerProcess = null; });
    return { running: true, port: p };
});

ipcMain.handle('chat:serverStop',   () => { if (chatServerProcess) { chatServerProcess.kill(); chatServerProcess = null; } return { running: false }; });
ipcMain.handle('chat:serverStatus', () => ({ running: !!chatServerProcess }));

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
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
