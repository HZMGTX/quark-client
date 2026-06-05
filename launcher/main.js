'use strict';
const { app, BrowserWindow, ipcMain, shell } = require('electron');
const path = require('path');
const http = require('http');
const { exec } = require('child_process');
const Store  = require('./store');

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
        width          : 860,
        height         : 580,
        resizable      : false,
        frame          : false,
        transparent    : true,
        backgroundColor: '#00000000',
        webPreferences : {
            preload            : path.join(__dirname, 'preload.js'),
            contextIsolation   : true,
            nodeIntegration    : false,
        },
    });

    win.loadFile(path.join(__dirname, 'src', 'index.html'));

    if (process.env.NODE_ENV === 'development') {
        win.webContents.openDevTools({ mode: 'detach' });
    }
}

app.whenReady().then(() => {
    createWindow();
    app.on('activate', () => { if (BrowserWindow.getAllWindows().length === 0) createWindow(); });
});

app.on('window-all-closed', () => { if (process.platform !== 'darwin') app.quit(); });

// ─────────────────────────────────────────────────────────────────────────────
// IPC – window controls
// ─────────────────────────────────────────────────────────────────────────────

ipcMain.handle('window:minimize', () => win.minimize());
ipcMain.handle('window:close',    () => app.quit());
ipcMain.handle('app:version',     () => app.getVersion());

// ─────────────────────────────────────────────────────────────────────────────
// IPC – settings
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

    if (!clientId) {
        throw new Error('NO_CLIENT_ID');
    }

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
                    res.writeHead(400, { 'Content-Type': 'text/html' });
                    res.end(callbackHtml('Error', errMsg || 'No code returned.', '#FF5555'));
                    clearTimeout(timeout);
                    server.close();
                    reject(new Error(errMsg || 'No code'));
                    return;
                }

                // Exchange code for token
                const tokenRes = await fetch('https://discord.com/api/oauth2/token', {
                    method : 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body   : new URLSearchParams({
                        client_id    : clientId,
                        client_secret: clientSecret,
                        grant_type   : 'authorization_code',
                        code,
                        redirect_uri : REDIRECT_URI,
                    }),
                });

                if (!tokenRes.ok) {
                    const body = await tokenRes.text();
                    res.writeHead(500, { 'Content-Type': 'text/html' });
                    res.end(callbackHtml('Token error', body, '#FF5555'));
                    clearTimeout(timeout);
                    server.close();
                    reject(new Error('Token exchange failed: ' + body));
                    return;
                }

                const tokenData = await tokenRes.json();

                // Fetch Discord user info
                const userRes = await fetch('https://discord.com/api/users/@me', {
                    headers: { Authorization: `Bearer ${tokenData.access_token}` },
                });
                const user = await userRes.json();

                user.avatarUrl = user.avatar
                    ? `https://cdn.discordapp.com/avatars/${user.id}/${user.avatar}.png?size=128`
                    : `https://cdn.discordapp.com/embed/avatars/${Number(user.id) % 5}.png`;

                store.set('user',        user);
                store.set('accessToken', tokenData.access_token);

                res.writeHead(200, { 'Content-Type': 'text/html' });
                res.end(callbackHtml(
                    'Logged in!',
                    `Welcome, <strong>${user.username}</strong>. You can close this tab.`,
                    '#55FF55'
                ));

                clearTimeout(timeout);
                server.close();
                resolve(user);

            } catch (err) {
                clearTimeout(timeout);
                try { server.close(); } catch (_) {}
                reject(err);
            }
        });

        server.on('error', (err) => { clearTimeout(timeout); reject(err); });
        server.listen(3847, () => shell.openExternal(authUrl));
    });
});

ipcMain.handle('discord:logout', () => {
    store.delete('user');
    store.delete('accessToken');
    return true;
});

// ─────────────────────────────────────────────────────────────────────────────
// IPC – injection
// ─────────────────────────────────────────────────────────────────────────────

ipcMain.handle('inject:scan', () => {
    return new Promise((resolve) => {
        const isWin = process.platform === 'win32';
        const cmd   = isWin
            ? 'tasklist /fo csv /nh'
            : 'ps -eo pid,comm,args';

        exec(cmd, (err, stdout) => {
            if (err) { resolve([]); return; }

            const results = [];

            if (isWin) {
                for (const line of stdout.split('\n')) {
                    const m = line.match(/"([^"]+)","(\d+)"/);
                    if (m && /javaw?\.exe/i.test(m[1])) {
                        results.push({ pid: parseInt(m[2], 10), name: m[1] });
                    }
                }
            } else {
                for (const line of stdout.split('\n').slice(1)) {
                    const parts = line.trim().split(/\s+/);
                    if (parts.length >= 2 && /^java$/i.test(parts[1])) {
                        results.push({ pid: parseInt(parts[0], 10), name: parts.slice(1).join(' ') });
                    }
                }
            }

            resolve(results);
        });
    });
});

const fs = require('fs');

ipcMain.handle('inject:run', async (_e, pid) => {
    console.log(`[Inject] Attaching to PID ${pid}`);
    
    return new Promise((resolve, reject) => {
        const injectorExe = path.join(__dirname, '..', 'quark-cpp', 'build', 'Release', 'quark-injector.exe');
        
        if (!fs.existsSync(injectorExe)) {
            return reject(new Error("C++ Injector not found. Please compile it first."));
        }

        // 1. First, ALWAYS stage the Fabric JAR so their modules actually load on next restart
        // Dynamically resolve --gameDir from the running process to support ALL launchers
        exec(`wmic process where processid=${pid} get commandline`, (errCmd, stdoutCmd) => {
            let gameDir = null;
            if (!errCmd && stdoutCmd) {
                // Match --gameDir "path" or --gameDir path
                const match = stdoutCmd.match(/--gameDir\s+"?([^"]+)"?/);
                if (match && match[1]) {
                    gameDir = match[1].trim();
                } else {
                    // Fallback to extracting from -Djava.library.path if possible, or just default
                    const libMatch = stdoutCmd.match(/-Djava\.library\.path="?([^"]+)"?/);
                    if (libMatch && libMatch[1]) {
                        // Usually something like .minecraft/bin/natives, we can traverse up
                        gameDir = path.resolve(libMatch[1], '..', '..');
                    }
                }
            }

            let modsFolder;
            if (gameDir) {
                modsFolder = path.join(gameDir, 'mods');
                console.log(`[Inject] Detected GameDir from launcher: ${gameDir}`);
            } else {
                const appdata = process.env.APPDATA || (process.platform == 'darwin' ? process.env.HOME + '/Library/Application Support' : process.env.HOME + "/.local/share");
                modsFolder = path.join(appdata, '.minecraft', 'mods'); // Vanilla fallback
                console.log(`[Inject] GameDir not found in args, falling back to: ${modsFolder}`);
            }

            if (!fs.existsSync(modsFolder)) fs.mkdirSync(modsFolder, { recursive: true });
            const jarSource = path.join(__dirname, '..', 'versions', '1.21.1', 'build', 'libs', 'quark-1.21.1-1.0.0+mc1.21.1.jar');
            const jarDest = path.join(modsFolder, 'quark-1.21.1-1.0.0+mc1.21.1.jar');
            
            try {
                if (fs.existsSync(jarSource)) fs.copyFileSync(jarSource, jarDest);
            } catch (e) { console.error("Could not stage JAR:", e); }
            
            // 2. Second, execute the native C++ injector for true Ghost Client runtime injection
            exec(`"${injectorExe}"`, { cwd: path.dirname(injectorExe) }, (err, stdout, stderr) => {
                console.log(stdout);
                if (err || stdout.includes('Failed') || stderr.includes('Failed')) {
                    console.error(stderr);
                    return reject(new Error("C++ Injection failed. Make sure Minecraft is running."));
                }
                
                // True Ghost Client Injection successful!
                resolve({ success: true, pid: pid, gameDir: gameDir || 'Vanilla .minecraft', requiresRestart: false });
            });
        });
        
    });
});

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

function callbackHtml(heading, body, color) {
    return `<!DOCTYPE html><html><head><meta charset="UTF-8">
<style>
  *{margin:0;padding:0;box-sizing:border-box}
  body{background:#0D0D0F;color:#fff;font-family:"Segoe UI",sans-serif;
       display:flex;align-items:center;justify-content:center;
       height:100vh;flex-direction:column;gap:12px}
  h2{color:${color};font-size:22px}
  p{color:#888;font-size:14px}
  strong{color:#fff}
</style></head><body>
<h2>${heading}</h2><p>${body}</p>
</body></html>`;
}
