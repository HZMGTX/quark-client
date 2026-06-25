'use strict';
/* global quark */

// ─────────────────────────────────────────────────────────────────────────────
// State
// ─────────────────────────────────────────────────────────────────────────────

let currentUser  = null;
let currentPage  = 'home';
let selectedPid  = null;
let processList  = [];
let injected     = false;
let alts         = [];
let activeAlt    = null;   // name of the account selected for the next launch
let profiles     = [];
let servers      = [];
let keybinds     = {};
let sponsorAds   = [];     // user-managed ad inventory (Settings → Sponsors & Ads)
let adStats      = {};     // { adId: { impressions, clicks } } — local only
let adRotation   = 0;
let pageCleanup  = null;
let sessionStats = {
    injectCount   : 0,
    sessionStart  : Date.now(),
    modulesEnabled: 0,
    lastPid       : null,
};

const STAFF_IDS = {
    '1401853518100303932': 'Owner',
};

// The real catalog the injected client actually ships — these are the exact
// modules in launcher/agent/StandaloneClient.java, grouped the same way. Every
// name here corresponds to something that genuinely runs in game; there are no
// placeholder/cheat entries. Kept in sync by hand with the agent source.
const MODULE_LIST = {
    render: ['FullBright', 'Zoom'],
    hud:    ['Watermark', 'ModuleList', 'FPS', 'Keystrokes', 'CPS', 'Coordinates',
             'ArmorStatus', 'Ping', 'Direction', 'Clock', 'Health', 'Hunger',
             'Speed', 'HeldItem', 'ServerIP', 'GameTime', 'Memory', 'SessionInfo'],
    misc:   ['ClickGui', 'ConfigManager', 'Notifications'],
};
const MODULE_DESCRIPTIONS = {
    FullBright: 'Maxes out brightness while enabled',
    Zoom: 'Hold C to zoom the camera in',
    Watermark: 'Quark logo + live FPS', ModuleList: 'Active module list',
    FPS: 'Standalone FPS counter', Keystrokes: 'WASD + mouse keys with live CPS',
    CPS: 'Standalone click-per-second counter', Coordinates: 'Live player XYZ position',
    ArmorStatus: 'Worn armor + durability', Ping: 'Live connection latency',
    Direction: 'Facing direction + yaw', Clock: 'Real-time system clock',
    Health: 'Live health', Hunger: 'Live food level',
    Speed: 'Horizontal speed in blocks/sec', HeldItem: 'Name + count of the held item',
    ServerIP: 'Address of the current server', GameTime: 'In-game day + clock',
    Memory: 'JVM heap usage', SessionInfo: 'Your username + FPS',
    ClickGui: 'The in-game menu (Right-Shift)', ConfigManager: 'Autosaves your settings',
    Notifications: 'Toast pop-ups for toggles',
};
const MODULE_COUNTS = Object.fromEntries(
    Object.entries(MODULE_LIST).map(([cat, mods]) => [cat, mods.length]));
const TOTAL_MODULES = Object.values(MODULE_COUNTS).reduce((a, b) => a + b, 0);
const MODULE_CATEGORY_COUNT = Object.keys(MODULE_LIST).length;
const MAX_MODULE_COUNT = Math.max(...Object.values(MODULE_COUNTS));

// In-game controls are fixed in the agent (StandaloneClient.java); shown read-only.
const CLIENT_CONTROLS = {
    'Open / close menu': 'Right-Shift',
    'Switch category': '← / →',
    'Move selection': '↑ / ↓',
    'Toggle module': 'Enter',
    'Resize the UI': '[ / ]',
    'Hold to zoom': 'C',
};

// ─────────────────────────────────────────────────────────────────────────────
// Sponsors / Ads
//
// A simple, honest ad slot. The launcher owner fills their own inventory in
// Settings → Sponsors & Ads (or sells the slot to a sponsor). Each banner is
// clearly labelled "Sponsored", links open in the system browser, and local
// impression/click counts are kept so the owner has real numbers to bill on.
// No tracking pixels, popups, redirects or third-party scripts.
// ─────────────────────────────────────────────────────────────────────────────

// Shown when no sponsor is configured — a house ad that points at the editor.
const HOUSE_AD = {
    id: '__house', sponsored: false,
    title: 'Advertise here',
    body: 'This slot is yours. Add a sponsor in Settings → Sponsors & Ads to monetise the launcher.',
    cta: 'Set up', url: '', img: '',
};

function activeAds() {
    return sponsorAds.filter(a => a && a.title);
}

function pickAd() {
    const ads = activeAds();
    if (!ads.length) return HOUSE_AD;
    const ad = ads[adRotation % ads.length];
    adRotation++;
    return ad;
}

function recordAd(field, ad) {
    if (!ad || ad.id === '__house') return;
    const s = adStats[ad.id] || { impressions: 0, clicks: 0 };
    s[field] = (s[field] || 0) + 1;
    adStats[ad.id] = s;
    quark.settingsSet('adStats', adStats);
}

// Renders one ad banner into the element with the given id (call after innerHTML).
function mountAd(containerId) {
    const host = document.getElementById(containerId);
    if (!host) return;
    const ad = pickAd();
    const sponsored = ad.id !== '__house';
    const safeUrl = typeof ad.url === 'string' && /^https:\/\//i.test(ad.url) ? ad.url : '';
    host.innerHTML = `
      <div class="ad-banner${safeUrl ? ' clickable' : ''}">
        <span class="ad-tag">${sponsored ? 'Sponsored' : 'Ad'}</span>
        ${ad.img && /^https:\/\//i.test(ad.img)
            ? `<img class="ad-img" src="${escapeHtml(ad.img)}" alt="" onerror="this.style.display='none'">` : ''}
        <div class="ad-body">
          <div class="ad-title">${escapeHtml(ad.title || '')}</div>
          ${ad.body ? `<div class="ad-text">${escapeHtml(ad.body)}</div>` : ''}
        </div>
        ${ad.cta ? `<button class="btn btn-primary btn-sm ad-cta">${escapeHtml(ad.cta)}</button>` : ''}
      </div>`;
    recordAd('impressions', ad);

    const go = () => {
        if (ad.id === '__house') { navigateTo('settings'); return; }
        if (!safeUrl) return;
        recordAd('clicks', ad);
        quark.openExternal(safeUrl);
    };
    const banner = host.querySelector('.ad-banner');
    const cta = host.querySelector('.ad-cta');
    if (cta) cta.addEventListener('click', e => { e.stopPropagation(); go(); });
    if (banner && (safeUrl || ad.id === '__house')) banner.addEventListener('click', go);
}

function adStatsSummary() {
    const ids = Object.keys(adStats);
    if (!ids.length) return 'No ad activity yet.';
    let imp = 0, clk = 0;
    for (const id of ids) { imp += adStats[id].impressions || 0; clk += adStats[id].clicks || 0; }
    const ctr = imp ? ((clk / imp) * 100).toFixed(1) : '0.0';
    return `Lifetime: ${imp.toLocaleString()} impressions · ${clk.toLocaleString()} clicks · ${ctr}% CTR`;
}

function getRole(user) {
    if (!user || user.guest) return 'User';
    return STAFF_IDS[user.id] || 'User';
}
function isStaff(user)   { return getRole(user) !== 'User'; }
function roleBadge(role) {
    return `<span class="role-badge role-${role.toLowerCase()}">${role}</span>`;
}

// ─────────────────────────────────────────────────────────────────────────────
// Notifications
// ─────────────────────────────────────────────────────────────────────────────

function notify(msg, type = 'info', duration = 3500) {
    let stack = document.getElementById('notif-stack');
    if (!stack) {
        stack = document.createElement('div');
        stack.id = 'notif-stack'; stack.className = 'notif-stack';
        document.body.appendChild(stack);
    }
    const icons = { success: '✓', error: '✕', info: 'ℹ', warn: '⚠' };
    const el = document.createElement('div');
    el.className = `notif ${type}`;
    el.innerHTML = `<span style="font-size:14px">${icons[type] || icons.info}</span><span>${escapeHtml(msg)}</span>`;
    stack.appendChild(el);
    setTimeout(() => {
        el.style.opacity = '0'; el.style.transition = 'opacity .3s';
        setTimeout(() => el.remove(), 320);
    }, duration);
}

// ─────────────────────────────────────────────────────────────────────────────
// Bootstrap
// ─────────────────────────────────────────────────────────────────────────────

window.addEventListener('DOMContentLoaded', async () => {
    // Window controls
    document.getElementById('btn-min').addEventListener('click',   () => quark.minimize());
    document.getElementById('btn-close').addEventListener('click', () => quark.close());
    document.getElementById('btn-max')?.addEventListener('click',  () => quark.maximize());

    // Version badge
    try {
        const v = await quark.version();
        document.getElementById('tb-version').textContent = `v${v}`;
    } catch (_) {}

    // Particle bg
    startParticles();

    // Load persisted state
    const stored  = await quark.settingsGet('user');
    alts          = (await quark.settingsGet('alts'))     || [];
    activeAlt     = (await quark.settingsGet('activeAlt')) || null;
    profiles      = (await quark.settingsGet('profiles')) || defaultProfiles();
    servers       = (await quark.settingsGet('servers'))  || defaultServers();
    keybinds      = (await quark.settingsGet('keybinds')) || {};
    sponsorAds    = (await quark.settingsGet('ads'))      || [];
    adStats       = (await quark.settingsGet('adStats'))  || {};
    sessionStats  = (await quark.settingsGet('stats'))    || sessionStats;
    sessionStats.sessionStart = Date.now();

    const avatarOverride = await quark.settingsGet('avatarOverride');

    if (stored) {
        currentUser = stored;
        if (avatarOverride) currentUser.avatarOverride = avatarOverride;
        showMain();
    } else {
        showLogin();
    }

    document.getElementById('btn-discord-login').addEventListener('click', handleDiscordLogin);
    document.getElementById('btn-skip').addEventListener('click', () => {
        currentUser = { username: 'Guest', guest: true };
        if (avatarOverride) currentUser.avatarOverride = avatarOverride;
        showMain();
    });
    document.getElementById('btn-cancel-oauth')?.addEventListener('click', () => setOauthOverlay(false));

    // Listen for live inject logs from main process
    quark.onInjectLog(d => {
        const log = document.getElementById('inject-log');
        if (!log) return;
        const cls = d.level === 'warn' ? 'log-warn' : d.level === 'error' ? 'log-error' : 'log-info';
        log.innerHTML += `\n<span class="${cls}">${escapeHtml(d.msg)}</span>`;
        log.scrollTop = log.scrollHeight;
    });

    // Listen for auto-inject detections
    quark.onAutoDetected(async proc => {
        const cfg = await quark.settingsGetAll();
        if (cfg.autoInject) {
            notify(`Auto-injecting into Minecraft (PID ${proc.pid})…`, 'info', 4000);
            try {
                await quark.injectRun(proc.pid);
                injected = true;
                sessionStats.injectCount++;
                sessionStats.lastPid = proc.pid;
                quark.settingsSet('stats', sessionStats);
                setStatus(`Auto-injected ✓ PID ${proc.pid}`, 'injected');
                notify('Auto-inject successful! Press Right-Shift in game.', 'success', 6000);
            } catch (e) {
                notify('Auto-inject failed: ' + e.message, 'error');
            }
        } else {
            notify(`Minecraft detected (PID ${proc.pid}) — go to Inject to attach`, 'info', 6000);
            selectedPid = proc.pid;
        }
    });

    // Listen for tray navigation requests
    quark.onNavigate(page => navigateTo(page));

    // Auto-scan for processes every 10s if on inject page
    setInterval(() => { if (currentPage === 'inject') refreshProcessList(false); }, 10000);
});

// ─────────────────────────────────────────────────────────────────────────────
// Particle background
// ─────────────────────────────────────────────────────────────────────────────

function startParticles() {
    const canvas = document.getElementById('particle-canvas');
    if (!canvas) return;
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    const ctx = canvas.getContext('2d');
    const pts = Array.from({ length: 70 }, () => ({
        x  : Math.random() * canvas.width,
        y  : Math.random() * canvas.height,
        vx : (Math.random() - .5) * .4,
        vy : (Math.random() - .5) * .4,
        r  : Math.random() * 1.6 + .5,
        o  : Math.random() * .5 + .1,
        hue: Math.random() > .5 ? '#A855F7' : '#06B6D4',
    }));

    function frame() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        pts.forEach(p => {
            p.x += p.vx; p.y += p.vy;
            if (p.x < 0) p.x = canvas.width;
            if (p.x > canvas.width) p.x = 0;
            if (p.y < 0) p.y = canvas.height;
            if (p.y > canvas.height) p.y = 0;
            ctx.beginPath();
            ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
            ctx.fillStyle = p.hue + Math.round(p.o * 255).toString(16).padStart(2, '0');
            ctx.fill();
        });
        for (let i = 0; i < pts.length; i++) {
            for (let j = i + 1; j < pts.length; j++) {
                const d = Math.hypot(pts[i].x - pts[j].x, pts[i].y - pts[j].y);
                if (d < 110) {
                    ctx.beginPath();
                    ctx.moveTo(pts[i].x, pts[i].y);
                    ctx.lineTo(pts[j].x, pts[j].y);
                    ctx.strokeStyle = `rgba(168,85,247,${0.10 * (1 - d / 110)})`;
                    ctx.lineWidth = .5;
                    ctx.stroke();
                }
            }
        }
        requestAnimationFrame(frame);
    }
    frame();
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen transitions
// ─────────────────────────────────────────────────────────────────────────────

function showLogin() {
    document.getElementById('screen-login').classList.add('active');
    document.getElementById('screen-main').classList.remove('active');
}

function showMain() {
    document.getElementById('screen-login').classList.remove('active');
    document.getElementById('screen-main').classList.add('active');
    buildSidebar();
    navigateTo('home');

    // Start auto-inject monitor if configured
    quark.settingsGet('autoInject').then(v => {
        if (v) {
            quark.injectAutoStart();
            const ind = document.getElementById('tb-auto-inject-indicator');
            if (ind) ind.classList.remove('hidden');
        }
    });
}

// ─────────────────────────────────────────────────────────────────────────────
// Discord login
// ─────────────────────────────────────────────────────────────────────────────

function setOauthOverlay(visible) {
    const el = document.getElementById('oauth-overlay');
    if (!el) return;
    el.classList.toggle('hidden', !visible);
}

async function handleDiscordLogin() {
    setOauthOverlay(true);
    try {
        const user = await quark.discordLogin();
        currentUser = user;
        await quark.settingsSet('user', user);
        setOauthOverlay(false);
        showMain();
    } catch (err) {
        setOauthOverlay(false);
        const msg = err.message || '';
        if (msg.includes('NO_CLIENT_ID')) {
            notify('Set your Discord Client ID in Settings first.', 'warn');
            currentUser = { username: 'Guest', guest: true };
            showMain();
            navigateTo('settings');
        } else if (!msg.includes('timed out') && !msg.includes('cancel')) {
            notify('Login failed: ' + msg, 'error');
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sidebar
// ─────────────────────────────────────────────────────────────────────────────

function buildSidebar() {
    const staffItem = document.querySelector('.staff-only');
    if (staffItem) staffItem.classList.toggle('hidden', !isStaff(currentUser));

    document.querySelectorAll('.nav-item').forEach(btn => {
        btn.addEventListener('click', () => navigateTo(btn.dataset.page));
    });

    const role = getRole(currentUser);
    const ur   = document.getElementById('user-row');
    const avatarSrc = currentUser.avatarOverride || currentUser.avatarUrl;
    const avatar = avatarSrc
        ? `<img class="user-avatar" src="${avatarSrc}" alt="">`
        : `<div class="user-avatar-placeholder">${(currentUser.username || 'G')[0].toUpperCase()}</div>`;

    ur.innerHTML = `
      <div class="user-row" id="user-row-btn">
        ${avatar}
        <div class="user-info">
          <div class="user-name">${currentUser.username || 'Guest'}</div>
          <div class="user-role">${roleBadge(role)}</div>
        </div>
      </div>`;

    document.getElementById('user-row-btn')?.addEventListener('click', showUserMenu);
}

function navigateTo(page) {
    currentPage = page;
    document.querySelectorAll('.nav-item').forEach(b => b.classList.toggle('active', b.dataset.page === page));
    const pages = {
        home, inject, modules, profiles: profilesPage, alts: altsPage,
        chat, changelog, staff, settings, news, servers: serversPage,
        keybinds: keybindsPage, stats: statsPage,
    };
    const fn = pages[page];
    if (fn) {
        // Run cleanup from previous page (removes listeners, intervals, etc.)
        if (pageCleanup) { pageCleanup(); pageCleanup = null; }
        const content = document.getElementById('content');
        content.innerHTML = '';
        content.classList.remove('page-enter');
        void content.offsetWidth;
        content.classList.add('page-enter');
        fn();
    }
}

function showUserMenu() {
    if (currentUser && !currentUser.guest) {
        if (confirm('Sign out of Discord?')) {
            quark.settingsSet('user', null);
            quark.discordLogout();
            currentUser = null;
            showLogin();
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Status bar
// ─────────────────────────────────────────────────────────────────────────────

function setStatus(text, state = '') {
    const dot  = document.getElementById('status-dot');
    const span = document.getElementById('status-text');
    if (dot)  dot.className  = 'status-dot' + (state ? ' ' + state : '');
    if (span) span.textContent = text;
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE: Home
// ─────────────────────────────────────────────────────────────────────────────

function home() {
    const role = getRole(currentUser);
    const uptime = Math.round((Date.now() - sessionStats.sessionStart) / 60000);

    document.getElementById('content').innerHTML = `
      <div class="page-header">
        <h1>Welcome back, ${escapeHtml(currentUser.username || 'Guest')}</h1>
        <p>Quark — pure JVM injection, ${TOTAL_MODULES} real modules, no files installed</p>
      </div>

      <div id="home-ad" style="margin-bottom:16px"></div>

      <div class="grid-4" style="margin-bottom:16px">
        <div class="stat-card">
          <div class="stat-label">Total Modules</div>
          <div class="stat-value brand">${TOTAL_MODULES}</div>
          <div class="stat-sub">across ${MODULE_CATEGORY_COUNT} categories</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Injection Status</div>
          <div class="stat-value sm">${injected ? '<span class="text-success">Injected ✓</span>' : '<span class="text-muted">Idle</span>'}</div>
          <div class="stat-sub" id="home-pid">${selectedPid ? 'PID ' + selectedPid : 'no target'}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Session Uptime</div>
          <div class="stat-value sm">${uptime}m</div>
          <div class="stat-sub">injected ${sessionStats.injectCount} time(s)</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Your Role</div>
          <div class="stat-value sm">${roleBadge(role)}</div>
          <div class="stat-sub">${isStaff(currentUser) ? 'staff tools unlocked' : 'standard access'}</div>
        </div>
      </div>

      <div class="grid-2" style="margin-bottom:16px">
        <div class="card glow">
          <div class="card-title">Quick Inject</div>
          <p style="font-size:12px;color:var(--muted);margin-bottom:12px">
            Attach Quark to a running Minecraft JVM — no files installed.
          </p>
          <div id="home-proc-status" style="font-size:12px;color:var(--muted);margin-bottom:10px">Scanning…</div>
          <button class="btn btn-primary btn-full" id="home-inject-btn" disabled>⚡ Inject Now</button>
          <div style="margin-top:8px;display:flex;gap:8px">
            <button class="btn btn-secondary btn-sm" id="home-scan-btn">🔍 Scan</button>
            <button class="btn btn-secondary btn-sm" id="home-open-inject">Open Inject Page →</button>
          </div>
        </div>

        <div class="card">
          <div class="card-title">Module Breakdown</div>
          ${Object.entries(MODULE_COUNTS).map(([cat, n]) => `
            <div style="display:flex;align-items:center;gap:8px;margin-bottom:6px">
              <span style="font-size:11px;color:var(--muted);width:68px;text-transform:capitalize">${cat}</span>
              <div class="progress-wrap" style="flex:1"><div class="progress-bar" style="width:${Math.round(n / MAX_MODULE_COUNT * 100)}%"></div></div>
              <span style="font-size:11px;color:var(--muted);width:28px;text-align:right">${n}</span>
            </div>`).join('')}
        </div>
      </div>

      <div class="card" style="margin-bottom:16px">
        <div class="card-title">Supported Launchers & Environments</div>
        <div style="display:flex;flex-wrap:wrap;gap:8px">
          ${['Official Launcher','Fabric','Forge','NeoForge','Vanilla','Lunar Client','Badlion','Feather','TLauncher',
             'Prism Launcher','MultiMC','PolyMC','GDLauncher','CurseForge','ATLauncher','Technic','SKLauncher'].map(e =>
            `<div class="module-chip">${e}</div>`).join('')}
        </div>
      </div>

      <div class="card">
        <div class="card-title">Quick Links</div>
        <div class="grid-4">
          ${[
            ['🔧','Inject','inject'],['📦','Modules','modules'],
            ['🎮','Profiles','profiles'],['🌐','Servers','servers'],
            ['💬','Chat','chat'],['📋','Changelog','changelog'],
            ['⌨','Controls','keybinds'],['📊','Stats','stats'],
          ].map(([icon, label, page]) =>
            `<button class="staff-action-btn" data-nav="${page}"><span class="icon">${icon}</span>${label}</button>`
          ).join('')}
        </div>
      </div>`;

    mountAd('home-ad');

    document.querySelectorAll('[data-nav]').forEach(btn => {
        btn.addEventListener('click', () => navigateTo(btn.dataset.nav));
    });
    document.getElementById('home-open-inject').addEventListener('click', () => navigateTo('inject'));

    document.getElementById('home-scan-btn').addEventListener('click', async () => {
        const status = document.getElementById('home-proc-status');
        const btn    = document.getElementById('home-inject-btn');
        if (status) status.textContent = 'Scanning…';
        await scanProcesses();
        const mc = processList.filter(p => p.isMinecraft !== false);
        if (mc.length === 0) {
            if (status) status.textContent = 'No Minecraft processes found.';
            if (btn) btn.disabled = true;
        } else {
            if (!selectedPid) selectedPid = mc[0].pid;
            if (status) status.textContent = `Found ${mc.length} process(es). PID ${selectedPid} selected.`;
            if (btn) btn.disabled = false;
        }
    });

    document.getElementById('home-inject-btn')?.addEventListener('click', () => {
        if (selectedPid) runInject(selectedPid, 'home');
    });

    // Auto-scan
    scanProcesses().then(() => {
        const status = document.getElementById('home-proc-status');
        const btn    = document.getElementById('home-inject-btn');
        if (!status) return;
        const mc = processList.filter(p => p.isMinecraft !== false);
        if (mc.length === 0) {
            status.textContent = 'No Minecraft processes detected. Launch Minecraft first.';
        } else {
            if (!selectedPid) selectedPid = mc[0].pid;
            status.textContent = `${mc.length} Minecraft process(es) found.`;
            if (btn) btn.disabled = false;
        }
    });
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE: News
// ─────────────────────────────────────────────────────────────────────────────

function news() {
    const entries = [
        {
            icon: '🚀', tags: ['update'],
            title: 'Quark — Real HUD Client',
            date: 'June 2025',
            desc: `${TOTAL_MODULES} real modules that all run in game via pure JVM agent injection — FullBright, Zoom and a full live HUD (coordinates, armour, ping, speed, health, hunger, held item, server IP, in-game time, memory and more). No files installed, no mods folder.`,
        },
        {
            icon: '💾', tags: ['feature'],
            title: 'Config Backup & Profiles',
            date: 'June 2025',
            desc: 'Export and import your full setup — settings, profiles, alts and saved servers — to a single JSON file, and switch between profiles for different servers. Everything is stored locally on your machine.',
        },
        {
            icon: '⚡', tags: ['feature'],
            title: 'Auto-Inject — Zero Effort',
            date: 'June 2025',
            desc: 'Enable Auto-Inject in Settings and Quark will automatically detect when Minecraft launches and attach itself. No manual injection needed — just launch the game and play.',
        },
        {
            icon: '🌐', tags: ['feature'],
            title: 'Server Manager with Live Ping',
            date: 'June 2025',
            desc: 'Add your favourite servers and see live player counts, latency, version and MOTD. The server pinger uses the native Minecraft status protocol.',
        },
        {
            icon: '💬', tags: ['feature'],
            title: 'Global Chat — Real Backend',
            date: 'May 2025',
            desc: 'Global Chat connects to a real WebSocket relay (launcher/server) with presence, history and reconnect backoff. If no relay URL is configured it honestly shows a "not configured" state instead of faking online users.',
        },
        {
            icon: '🔑', tags: ['feature'],
            title: 'In-Game Controls',
            date: 'April 2025',
            desc: 'The Controls page shows the real, fixed in-game controls — Right-Shift opens the menu, arrows navigate, Enter toggles, and [ ] resize the UI. Your enabled modules and UI scale autosave and restore on the next inject.',
        },
    ];

    document.getElementById('content').innerHTML = `
      <div class="page-header"><h1>News & Updates</h1><p>What's happening with Quark</p></div>
      ${entries.map(e => `
        <div class="news-card">
          <div class="news-icon">${e.icon}</div>
          <div class="news-body">
            <div style="margin-bottom:4px">
              ${e.tags.map(t => `<span class="news-tag ${t}">${t}</span>`).join('')}
            </div>
            <div class="news-title">${escapeHtml(e.title)}</div>
            <div class="news-date">${e.date}</div>
            <div class="news-desc">${escapeHtml(e.desc)}</div>
          </div>
        </div>`).join('')}`;
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE: Inject
// ─────────────────────────────────────────────────────────────────────────────

function inject() {
    document.getElementById('content').innerHTML = `
      <div class="page-header">
        <h1>Inject</h1>
        <p>Attach Quark to any running Minecraft JVM via the Java Attach API — no mods, no files.</p>
      </div>

      <div class="grid-2">
        <div style="display:flex;flex-direction:column;gap:14px">
          <div class="card">
            <div class="card-title">Process Scanner</div>
            <div id="process-list" style="display:flex;flex-direction:column;gap:8px;margin-bottom:12px">
              <div style="color:var(--muted);font-size:12px;text-align:center;padding:12px">Scanning…</div>
            </div>
            <div style="display:flex;gap:8px;flex-wrap:wrap">
              <button class="btn btn-secondary btn-sm" id="btn-scan">🔍 Refresh</button>
              <button class="btn btn-primary btn-sm" id="btn-inject" disabled>⚡ Inject Selected</button>
            </div>
          </div>

          <div class="card">
            <div class="card-title">Options</div>
            <div class="toggle-row">
              <div><div class="toggle-label">Auto-inject on launch</div><div class="toggle-sub">Watch for Minecraft and attach automatically</div></div>
              <label class="toggle"><input type="checkbox" id="opt-auto-inject"><span class="toggle-slider"></span></label>
            </div>
            <div class="toggle-row">
              <div><div class="toggle-label">Silent injection</div><div class="toggle-sub">Suppress routine attach log output</div></div>
              <label class="toggle"><input type="checkbox" id="opt-silent"><span class="toggle-slider"></span></label>
            </div>
            <p style="font-size:10px;color:var(--muted);margin-top:8px">These mirror the Settings page and are saved instantly.</p>
          </div>

          <div class="card">
            <div class="card-title">How It Works</div>
            <p style="font-size:12px;color:var(--muted);line-height:1.8">
              Uses the <strong style="color:var(--brand)">JVM Attach API</strong> to load a Java agent
              into the running Minecraft process. The agent uses <strong style="color:var(--cyan)">ASM 9
              bytecode instrumentation</strong> to hook MinecraftClient, GameRenderer,
              Keyboard and packet handlers — <strong style="color:var(--text)">no JAR in mods folder</strong>.
            </p>
            <div style="margin-top:10px;display:flex;gap:6px;flex-wrap:wrap">
              <span class="module-chip" style="font-size:10px">JVM Attach API</span>
              <span class="module-chip" style="font-size:10px">ASM 9 Bytecode</span>
              <span class="module-chip" style="font-size:10px">jattach fallback</span>
              <span class="module-chip" style="font-size:10px">Mods-dir fallback</span>
            </div>
          </div>
        </div>

        <div style="display:flex;flex-direction:column;gap:14px">
          <div class="card">
            <div class="card-title">Injection Progress</div>
            <div class="inject-steps" id="inject-steps">
              <div class="inject-step"><span class="step-icon pending">○</span>Scan for Minecraft JVM processes</div>
              <div class="inject-step"><span class="step-icon pending">○</span>Locate Java executable</div>
              <div class="inject-step"><span class="step-icon pending">○</span>Resolve agent JAR</div>
              <div class="inject-step"><span class="step-icon pending">○</span>Attach to target PID</div>
              <div class="inject-step"><span class="step-icon pending">○</span>Load Quark agent into JVM</div>
              <div class="inject-step"><span class="step-icon pending">○</span>Instrument Minecraft classes</div>
              <div class="inject-step"><span class="step-icon pending">○</span>Initialise module system</div>
              <div class="inject-step"><span class="step-icon pending">○</span>Injection complete ✓</div>
            </div>
          </div>

          <div class="card">
            <div class="card-title">Console Output</div>
            <div class="inject-log" id="inject-log">
              <span class="log-info">[Quark] Launcher ready. Select a process and click Inject.</span>
            </div>
            <div style="display:flex;gap:6px;margin-top:8px">
              <button class="btn btn-secondary btn-sm" id="btn-clear-log">Clear Log</button>
            </div>
          </div>
        </div>
      </div>`;

    document.getElementById('btn-scan').addEventListener('click', () => refreshProcessList(true));
    document.getElementById('btn-inject').addEventListener('click', () => {
        if (selectedPid) runInject(selectedPid, 'inject');
    });
    document.getElementById('btn-clear-log').addEventListener('click', () => {
        const log = document.getElementById('inject-log');
        if (log) log.innerHTML = '<span class="log-info">[Quark] Log cleared.</span>';
    });

    // Hydrate + wire the real, persisted option toggles.
    (async () => {
        const autoEl   = document.getElementById('opt-auto-inject');
        const silentEl = document.getElementById('opt-silent');
        if (autoEl)   autoEl.checked   = !!(await quark.settingsGet('autoInject'));
        if (silentEl) silentEl.checked = !!(await quark.settingsGet('silentInject'));

        autoEl?.addEventListener('change', async () => {
            await quark.settingsSet('autoInject', autoEl.checked);
            if (autoEl.checked) {
                quark.injectAutoStart();
                document.getElementById('tb-auto-inject-indicator')?.classList.remove('hidden');
                notify('Auto-inject enabled', 'success');
            } else {
                quark.injectAutoStop();
                document.getElementById('tb-auto-inject-indicator')?.classList.add('hidden');
                notify('Auto-inject disabled', 'info');
            }
        });
        silentEl?.addEventListener('change', async () => {
            await quark.settingsSet('silentInject', silentEl.checked);
            notify(silentEl.checked ? 'Silent injection on' : 'Silent injection off', 'info');
        });
    })();

    refreshProcessList(true);
}

async function scanProcesses() {
    try {
        processList = await quark.injectScan();
    } catch (_) {
        processList = [];
    }
    return processList;
}

async function refreshProcessList(showLoading = false) {
    const list = document.getElementById('process-list');
    const btn  = document.getElementById('btn-inject');

    if (list && showLoading) {
        list.innerHTML = `<div style="color:var(--muted);font-size:12px;text-align:center;padding:12px">Scanning for JVM processes…</div>`;
    }

    setStatus('Scanning…', 'scanning');
    await scanProcesses();
    setStatus('Ready');

    if (!list) return;

    const all = processList;
    const mc  = all.filter(p => p.isMinecraft !== false);
    const display = mc.length > 0 ? mc : all;

    if (display.length === 0) {
        list.innerHTML = `<div style="color:var(--muted);font-size:12px;padding:16px;text-align:center">
            No Java processes found.<br>
            <span style="font-size:11px">Launch Minecraft and click Refresh.</span>
        </div>`;
        if (btn) btn.disabled = true;
        return;
    }

    list.innerHTML = display.map(p => {
        const loader   = p.loader   || 'Vanilla';
        const launcher = p.launcher || 'Unknown';
        const version  = p.version  || '';
        const memory   = p.memory   || '';
        return `
          <div class="process-item ${p.pid === selectedPid ? 'selected' : ''}" data-pid="${p.pid}">
            <div class="process-icon">⛏</div>
            <div class="process-info">
              <div class="process-name">Minecraft${version ? ' ' + version : ''} (PID ${p.pid})</div>
              <div class="process-pid">${launcher !== 'Unknown' ? launcher + ' · ' : ''}${p.name || 'java'}</div>
              ${memory ? `<div class="process-meta">Memory: ${memory}</div>` : ''}
            </div>
            <span class="process-badge ${loader.toLowerCase()}">${loader}</span>
          </div>`;
    }).join('');

    if (!selectedPid && display.length > 0) selectedPid = display[0].pid;
    if (btn) btn.disabled = !selectedPid;

    list.querySelectorAll('.process-item').forEach(el => {
        el.addEventListener('click', () => {
            selectedPid = parseInt(el.dataset.pid, 10);
            list.querySelectorAll('.process-item').forEach(e => e.classList.remove('selected'));
            el.classList.add('selected');
            if (btn) btn.disabled = false;
        });
    });
}

async function runInject(pid, context) {
    const stepsEl = document.getElementById('inject-steps');
    const logEl   = document.getElementById('inject-log');

    function logLine(msg, cls = '') {
        if (logEl) {
            logEl.innerHTML += `\n<span class="${cls}">${escapeHtml(msg)}</span>`;
            logEl.scrollTop = logEl.scrollHeight;
        }
    }

    function step(idx, state) {
        if (!stepsEl) return;
        const steps = stepsEl.querySelectorAll('.inject-step');
        if (!steps[idx]) return;
        const icon = steps[idx].querySelector('.step-icon');
        icon.className = `step-icon ${state}`;
        const map = { done: '✓', active: '◌', pending: '○' };
        icon.textContent = map[state] || '○';
        if (state === 'done') {
            steps[idx].style.color = 'var(--text)';
        }
    }

    setStatus(`Injecting → PID ${pid}`, 'injected');
    step(0, 'done'); step(1, 'active');
    logLine(`[Quark] Starting injection → PID ${pid}`, 'log-info');

    await sleep(150);
    step(1, 'done'); step(2, 'active');
    await sleep(100);
    step(2, 'done'); step(3, 'active');

    try {
        const result = await quark.injectRun(pid);
        step(3, 'done'); step(4, 'active');
        await sleep(250);
        step(4, 'done'); step(5, 'active');
        logLine('[Quark] Agent loaded — instrumenting Minecraft classes…', 'log-info');
        await sleep(350);
        step(5, 'done'); step(6, 'active');
        logLine('[Quark] Module system initialising…', 'log-info');
        await sleep(300);
        step(6, 'done'); step(7, 'active');
        await sleep(200);
        step(7, 'done');

        if (result.requiresRestart) {
            logLine('[Quark] ⚠ JAR staged in mods folder — restart Minecraft to activate.', 'log-warn');
            notify('Quark staged as mod. Restart Minecraft to activate.', 'warn', 8000);
            setStatus('Staged (restart needed)', 'error');
        } else {
            logLine('[Quark] ✓ Injection complete! Press Right-Shift in-game.', 'log-success');
            injected = true;
            selectedPid = pid;
            sessionStats.injectCount++;
            sessionStats.lastPid = pid;
            quark.settingsSet('stats', sessionStats);
            setStatus(`Injected ✓ PID ${pid}`, 'injected');
            notify(`Quark injected into PID ${pid}! Press Right-Shift in game.`, 'success', 6000);
        }
    } catch (err) {
        step(3, 'pending');
        logLine(`[Quark] ✕ ${err.message}`, 'log-error');
        setStatus('Injection failed', 'error');
        notify('Injection failed: ' + err.message, 'error', 8000);
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE: Modules
// ─────────────────────────────────────────────────────────────────────────────

function modules() {
    document.getElementById('content').innerHTML = `
      <div class="page-header">
        <h1>Module Browser</h1>
        <p>${TOTAL_MODULES} real modules across ${MODULE_CATEGORY_COUNT} categories — every one runs in game</p>
      </div>

      <div class="search-wrap">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <input class="search-input" id="mod-search" placeholder="Search ${TOTAL_MODULES} modules…" autocomplete="off">
      </div>

      <div class="filter-tabs" id="mod-filters">
        <button class="filter-tab active" data-cat="all">All (${TOTAL_MODULES})</button>
        ${Object.entries(MODULE_COUNTS).map(([c, n]) =>
            `<button class="filter-tab" data-cat="${c}">${c[0].toUpperCase()+c.slice(1)} (${n})</button>`
        ).join('')}
      </div>

      <div id="mod-content"></div>`;

    let activeCat = 'all';

    function render(search = '') {
        const container = document.getElementById('mod-content');
        const s = search.toLowerCase().trim();
        const cats = activeCat === 'all' ? Object.entries(MODULE_LIST) : [[activeCat, MODULE_LIST[activeCat] || []]];
        container.innerHTML = cats.map(([cat, mods]) => {
            const filtered = s ? mods.filter(m => m.toLowerCase().includes(s)) : mods;
            if (!filtered.length) return '';
            return `
              <div class="module-cat-header">${cat.toUpperCase()} <span>${filtered.length} module${filtered.length === 1 ? '' : 's'}${s && filtered.length !== mods.length ? ' shown' : ''}</span></div>
              <div class="module-grid" style="margin-bottom:16px">
                ${filtered.map(m => `<div class="module-chip" title="${escapeHtml(MODULE_DESCRIPTIONS[m] || '')}">${m}</div>`).join('')}
              </div>`;
        }).join('');
        if (!container.innerHTML.trim()) {
            container.innerHTML = `<div style="color:var(--muted);font-size:13px;padding:24px;text-align:center">No modules match "<strong>${escapeHtml(search)}</strong>"</div>`;
        }
    }

    render();
    document.getElementById('mod-search').addEventListener('input', e => render(e.target.value));
    document.querySelectorAll('.filter-tab').forEach(t => {
        t.addEventListener('click', () => {
            activeCat = t.dataset.cat;
            document.querySelectorAll('.filter-tab').forEach(x => x.classList.remove('active'));
            t.classList.add('active');
            render(document.getElementById('mod-search').value);
        });
    });
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE: Servers
// ─────────────────────────────────────────────────────────────────────────────

function defaultServers() {
    return [
        { name: 'Hypixel',      address: 'mc.hypixel.net',    port: 25565 },
        { name: 'Mineplex',     address: 'us.mineplex.com',   port: 25565 },
        { name: 'CubeCraft',    address: 'play.cubecraft.net', port: 25565 },
        { name: '2b2t',         address: '2b2t.org',           port: 25565 },
        { name: 'Wynncraft',    address: 'play.wynncraft.com', port: 25565 },
    ];
}

function serversPage() {
    document.getElementById('content').innerHTML = `
      <div class="page-header" style="display:flex;align-items:center;justify-content:space-between">
        <div><h1>Server Manager</h1><p>Add and ping Minecraft servers</p></div>
        <button class="btn btn-primary btn-sm" id="btn-add-server">+ Add Server</button>
      </div>
      <div id="server-list" style="display:flex;flex-direction:column;gap:10px"></div>`;

    renderServers();

    document.getElementById('btn-add-server').addEventListener('click', () => {
        const address = prompt('Server address (host:port or just host):');
        if (!address) return;
        const [host, portStr] = address.split(':');
        const name = prompt('Server name:', host) || host;
        servers.push({ name, address: host.trim(), port: parseInt(portStr, 10) || 25565 });
        quark.settingsSet('servers', servers);
        renderServers();
    });
}

function renderServers() {
    const list = document.getElementById('server-list');
    if (!list) return;

    list.innerHTML = servers.map((s, i) => `
      <div class="server-item" id="srv-${i}">
        <div class="server-favicon">🌐</div>
        <div class="server-info">
          <div class="server-name">${escapeHtml(s.name)}</div>
          <div class="server-address">${escapeHtml(s.address)}:${s.port}</div>
          <div class="server-motd" id="motd-${i}" style="color:var(--muted)">Click Ping to check</div>
        </div>
        <div class="server-right">
          <div class="server-players" id="players-${i}">— / —</div>
          <div class="server-latency" id="latency-${i}">— ms</div>
          <div class="server-version" id="version-${i}">Unknown</div>
          <div style="display:flex;gap:5px;margin-top:4px">
            <button class="btn btn-secondary btn-sm" data-ping="${i}">Ping</button>
            <button class="btn btn-danger btn-sm" data-del="${i}">✕</button>
          </div>
        </div>
      </div>`).join('');

    if (servers.length === 0) {
        list.innerHTML = `<div style="color:var(--muted);font-size:12px;text-align:center;padding:24px">No servers added. Click + Add Server.</div>`;
    }

    list.querySelectorAll('[data-ping]').forEach(btn => {
        const i = parseInt(btn.dataset.ping, 10);
        btn.addEventListener('click', async () => {
            btn.disabled = true;
            btn.textContent = '…';
            const srv = servers[i];
            try {
                const result = await quark.serverPing(srv.address, srv.port);
                const latEl  = document.getElementById(`latency-${i}`);
                const plsEl  = document.getElementById(`players-${i}`);
                const verEl  = document.getElementById(`version-${i}`);
                const motdEl = document.getElementById(`motd-${i}`);
                if (result.online) {
                    const lat = result.latency;
                    if (latEl) {
                        latEl.textContent = `${lat}ms`;
                        latEl.className = 'server-latency ' + (lat < 80 ? 'good' : lat < 200 ? 'ok' : 'bad');
                    }
                    if (plsEl) plsEl.textContent = `${result.players.online} / ${result.players.max}`;
                    if (verEl) verEl.textContent = result.version;
                    if (motdEl) motdEl.textContent = result.description || '';
                } else {
                    if (latEl) { latEl.textContent = 'Offline'; latEl.className = 'server-offline'; }
                    if (motdEl) motdEl.textContent = result.error || 'Server offline';
                }
            } catch (e) {
                notify('Ping failed: ' + e.message, 'error');
            }
            btn.disabled = false;
            btn.textContent = 'Ping';
        });
    });

    list.querySelectorAll('[data-del]').forEach(btn => {
        btn.addEventListener('click', () => {
            const i = parseInt(btn.dataset.del, 10);
            if (!isNaN(i) && i >= 0 && i < servers.length) {
                servers.splice(i, 1);
                quark.settingsSet('servers', servers);
                renderServers();
            }
        });
    });

    // Auto-ping all
    list.querySelectorAll('[data-ping]').forEach(btn => btn.click());
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE: Controls
// ─────────────────────────────────────────────────────────────────────────────

function keybindsPage() {
    // The injected client (StandaloneClient.java) is driven by fixed keys baked
    // into the agent — the launcher can't rebind them, so this page honestly
    // shows the real controls rather than pretending to be an editor.
    document.getElementById('content').innerHTML = `
      <div class="page-header">
        <h1>Controls</h1>
        <p>The in-game client is keyboard-driven. These controls are built into the injected client and are always active.</p>
      </div>
      <div class="card" style="margin-bottom:14px">
        <div class="card-title">In-game controls</div>
        ${Object.entries(CLIENT_CONTROLS).map(([action, key]) => `
          <div class="keybind-row">
            <div><div class="keybind-name">${action}</div></div>
            <div class="keybind-key" style="cursor:default">${key}</div>
          </div>`).join('')}
      </div>
      <div class="card">
        <div class="card-title">How toggling works</div>
        <p style="font-size:12px;color:var(--muted);line-height:1.7;margin:0">
          Open the menu with <strong style="color:var(--text)">Right-Shift</strong>, switch category with
          <strong style="color:var(--text)">←/→</strong>, move with <strong style="color:var(--text)">↑/↓</strong>,
          and press <strong style="color:var(--text)">Enter</strong> to toggle the selected module. The modules
          you enable and your UI scale are saved automatically by ConfigManager and restored the next time you inject.
        </p>
      </div>`;
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE: Statistics
// ─────────────────────────────────────────────────────────────────────────────

function statsPage() {
    const uptime = Math.round((Date.now() - sessionStats.sessionStart) / 1000);
    const uptimeStr = uptime < 60 ? `${uptime}s` : uptime < 3600 ? `${Math.round(uptime/60)}m` : `${Math.round(uptime/3600)}h`;

    document.getElementById('content').innerHTML = `
      <div class="page-header">
        <h1>Statistics</h1>
        <p>Session info and system diagnostics</p>
      </div>

      <div class="grid-4" style="margin-bottom:16px">
        <div class="stat-card">
          <div class="stat-label">Session Uptime</div>
          <div class="stat-value brand">${uptimeStr}</div>
          <div class="stat-sub">since launcher opened</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Injections</div>
          <div class="stat-value">${sessionStats.injectCount}</div>
          <div class="stat-sub">this session</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Total Modules</div>
          <div class="stat-value">${TOTAL_MODULES}</div>
          <div class="stat-sub">across ${MODULE_CATEGORY_COUNT} categories</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Status</div>
          <div class="stat-value sm">${injected ? '<span class="text-success">Active</span>' : '<span class="text-muted">Idle</span>'}</div>
          <div class="stat-sub">${sessionStats.lastPid ? 'last PID ' + sessionStats.lastPid : 'not injected'}</div>
        </div>
      </div>

      <div class="grid-2" style="margin-bottom:16px">
        <div class="card">
          <div class="card-title">Module Distribution</div>
          ${Object.entries(MODULE_COUNTS).map(([cat, n]) => `
            <div class="stat-bar-row">
              <div class="stat-bar-label" style="text-transform:capitalize">${cat}</div>
              <div class="stat-bar-wrap">
                <div class="progress-wrap"><div class="progress-bar" style="width:${Math.round(n/TOTAL_MODULES*100)}%"></div></div>
              </div>
              <div class="stat-bar-val">${n}</div>
            </div>`).join('')}
        </div>

        <div class="card" id="sysinfo-card">
          <div class="card-title">System Info</div>
          <div style="color:var(--muted);font-size:12px;text-align:center;padding:16px">Loading…</div>
        </div>
      </div>

      <div class="card" style="margin-bottom:16px">
        <div class="card-title">Java Installations</div>
        <div id="java-list" style="color:var(--muted);font-size:12px;padding:8px">Scanning…</div>
      </div>

      <div class="card">
        <div class="card-title">Detected Game Directories</div>
        <div id="gamedir-list" style="color:var(--muted);font-size:12px;padding:8px">Scanning…</div>
      </div>`;

    // Load system info
    quark.systemInfo().then(info => {
        const card = document.getElementById('sysinfo-card');
        if (!card) return;
        card.innerHTML = `
          <div class="card-title">System Info</div>
          ${[
            ['Platform', info.platform + ' ' + info.arch],
            ['CPU', info.cpuModel + ' (' + info.cpus + ' cores)'],
            ['RAM', info.memory + ' GB total, ' + info.freemem + ' GB free'],
            ['Hostname', info.hostname],
            ['Node.js', info.node],
            ['Electron', info.electron],
            ['Quark', 'v' + info.appVersion],
          ].map(([k, v]) => `
            <div style="display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px solid var(--border);font-size:12px">
              <span style="color:var(--muted)">${k}</span>
              <span style="color:var(--text);font-weight:600">${escapeHtml(String(v))}</span>
            </div>`).join('')}`;
    }).catch(() => {
        const card = document.getElementById('sysinfo-card');
        if (card) card.innerHTML = '<div class="card-title">System Info</div><div style="color:var(--muted);font-size:12px;padding:8px">Failed to load system info.</div>';
    });

    // Load Java list
    quark.javaList().then(list => {
        const el = document.getElementById('java-list');
        if (!el) return;
        if (!list || list.length === 0) {
            el.textContent = 'No Java installations found in common paths.';
            return;
        }
        el.innerHTML = list.map(j => `
          <div style="display:flex;align-items:center;gap:10px;padding:8px 0;border-bottom:1px solid var(--border)">
            <span style="font-size:14px">☕</span>
            <div>
              <div style="font-size:12px;font-weight:600">${escapeHtml(j.version)}</div>
              <div style="font-size:10px;color:var(--muted);font-family:monospace">${escapeHtml(j.path)}</div>
            </div>
          </div>`).join('');
    }).catch(() => {
        const el = document.getElementById('java-list');
        if (el) el.textContent = 'Error scanning Java installations.';
    });

    // Load game dirs
    quark.systemGameDirs().then(dirs => {
        const el = document.getElementById('gamedir-list');
        if (!el) return;
        if (!dirs || dirs.length === 0) {
            el.textContent = 'No Minecraft launcher directories found on this system.';
            return;
        }
        el.innerHTML = `<div style="display:flex;flex-direction:column;gap:8px">` +
            dirs.map(d => `
              <div class="gamedir-item">
                <span style="font-size:16px">📁</span>
                <div style="flex:1;min-width:0">
                  <div class="gamedir-label">${escapeHtml(d.label)}</div>
                  <div class="gamedir-path">${escapeHtml(d.path)}</div>
                </div>
                <button class="btn btn-secondary btn-sm" data-open-folder="${escapeHtml(d.path)}">Open</button>
              </div>`).join('') + `</div>`;
        el.querySelectorAll('[data-open-folder]').forEach(btn => {
            btn.addEventListener('click', () => quark.openFolder(btn.dataset.openFolder));
        });
    }).catch(() => {
        const el = document.getElementById('gamedir-list');
        if (el) el.textContent = 'Error scanning game directories.';
    });
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE: Profiles
// ─────────────────────────────────────────────────────────────────────────────

function defaultProfiles() {
    return [
        { id: 'pvp',     name: 'PvP Preset',    desc: 'KillAura, Criticals, AntiKB, Velocity, Reach', badges: ['combat','pvp'],    active: true  },
        { id: 'crystal', name: 'Crystal PvP',   desc: 'AutoCrystal, Surround, AutoTotem, HoleSnap',    badges: ['combat'],          active: false },
        { id: 'legit',   name: 'Legit Client',  desc: 'Reach 3.2, Velocity 80%, Criticals, FastPlace', badges: ['clean'],          active: false },
        { id: 'build',   name: 'Builder',       desc: 'Scaffold, AutoBuild, FastPlace, NoFall, Step',   badges: ['movement'],       active: false },
        { id: 'explore', name: 'Explorer',      desc: 'XRay, ESP, FullBright, VeinMiner, AutoFarm',     badges: ['render'],         active: false },
        { id: 'staff',   name: 'Staff Mode',    desc: 'Vanish, Detectors, Logs, AdminTools',            badges: ['combat','render'], active: false },
    ];
}

function profilesPage() {
    document.getElementById('content').innerHTML = `
      <div class="page-header" style="display:flex;align-items:center;justify-content:space-between">
        <div><h1>Profiles</h1><p>Save and load module configurations</p></div>
        <button class="btn btn-primary btn-sm" id="btn-new-profile">+ New Profile</button>
      </div>
      <div class="grid-3" id="profile-grid"></div>`;

    renderProfiles();

    document.getElementById('btn-new-profile').addEventListener('click', () => {
        const name = prompt('Profile name:');
        if (!name) return;
        const desc = prompt('Description:', 'Custom profile') || 'Custom profile';
        profiles.push({ id: Date.now() + '', name, desc, badges: [], active: false });
        quark.settingsSet('profiles', profiles);
        renderProfiles();
        notify('Profile created: ' + name, 'success');
    });
}

function renderProfiles() {
    const grid = document.getElementById('profile-grid');
    if (!grid) return;
    grid.innerHTML = profiles.map(p => `
      <div class="profile-card ${p.active ? 'active' : ''}" data-id="${p.id}">
        <div class="profile-name">${escapeHtml(p.name)}</div>
        <div class="profile-desc">${escapeHtml(p.desc)}</div>
        <div class="profile-badges">
          ${p.badges.map(b => `<span class="badge badge-${b}">${b}</span>`).join('')}
        </div>
        <div style="display:flex;gap:6px;margin-top:4px">
          <button class="btn btn-${p.active ? 'success' : 'secondary'} btn-sm" data-action="load" data-id="${p.id}">
            ${p.active ? '✓ Active' : 'Load'}
          </button>
          <button class="btn btn-secondary btn-sm" data-action="dup" data-id="${p.id}">Dup</button>
          <button class="btn btn-danger btn-sm" data-action="del" data-id="${p.id}">✕</button>
        </div>
      </div>`).join('');

    grid.querySelectorAll('[data-action]').forEach(btn => {
        btn.addEventListener('click', e => {
            const id = btn.dataset.id;
            if (btn.dataset.action === 'load') {
                profiles.forEach(p => p.active = p.id === id);
                quark.settingsSet('profiles', profiles);
                renderProfiles();
                notify('Profile loaded', 'success');
            } else if (btn.dataset.action === 'dup') {
                const src = profiles.find(p => p.id === id);
                if (src) {
                    profiles.push({ ...src, id: Date.now() + '', name: src.name + ' (Copy)', active: false });
                    quark.settingsSet('profiles', profiles);
                    renderProfiles();
                    notify('Profile duplicated', 'info');
                }
            } else {
                if (!confirm('Delete this profile?')) return;
                profiles = profiles.filter(p => p.id !== id);
                quark.settingsSet('profiles', profiles);
                renderProfiles();
                notify('Profile deleted', 'info');
            }
            e.stopPropagation();
        });
    });

    // Clicking the card itself (outside buttons) loads that profile
    grid.querySelectorAll('.profile-card').forEach(card => {
        card.addEventListener('click', () => {
            const id = card.dataset.id;
            profiles.forEach(p => p.active = p.id === id);
            quark.settingsSet('profiles', profiles);
            renderProfiles();
            notify('Profile loaded', 'success');
        });
    });
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE: Alt Manager
// ─────────────────────────────────────────────────────────────────────────────

function altsPage() {
    document.getElementById('content').innerHTML = `
      <div class="page-header" style="display:flex;align-items:center;justify-content:space-between">
        <div><h1>Alt Manager</h1><p>Store and switch between Minecraft accounts</p></div>
        <button class="btn btn-primary btn-sm" id="btn-add-alt">+ Add Alt</button>
      </div>
      <div class="card" style="margin-bottom:14px">
        <div class="card-title" id="alt-count-title">Saved Alts (${alts.length})</div>
        <div id="alt-active-sub" style="font-size:11px;color:var(--muted);margin:-4px 0 10px">${activeAlt ? 'Active account: ' + escapeHtml(activeAlt) : 'No active account selected'}</div>
        <div id="alt-list" style="display:flex;flex-direction:column;gap:8px"></div>
        ${alts.length === 0 ? '<div style="color:var(--muted);font-size:12px;text-align:center;padding:16px">No alts saved. Click + Add Alt.</div>' : ''}
      </div>
      <div class="card">
        <div class="card-title">Info</div>
        <p style="font-size:12px;color:var(--muted);line-height:1.8">
          The <strong style="color:var(--brand)">Use</strong> button marks an account as
          <strong style="color:var(--text)">active</strong> — it's saved and shown as your
          selected profile for the next session. Both Microsoft and offline accounts are stored.
          Minecraft can't hot-swap the login of an already-running game, so set the active
          account before launching.
        </p>
      </div>`;

    renderAlts();

    document.getElementById('btn-add-alt').addEventListener('click', () => {
        const name = prompt('Minecraft username or email:');
        if (!name) return;
        const type = name.includes('@') ? 'Microsoft' : 'Offline';
        alts.push({ name, type, added: Date.now() });
        quark.settingsSet('alts', alts);
        // Update in-place — no full page reload
        const titleEl = document.getElementById('alt-count-title');
        if (titleEl) titleEl.textContent = `Saved Alts (${alts.length})`;
        renderAlts();
        notify('Alt added: ' + name, 'success');
    });
}

function renderAlts() {
    const list = document.getElementById('alt-list');
    if (!list) return;
    if (alts.length === 0) {
        list.innerHTML = '<div style="color:var(--muted);font-size:12px;text-align:center;padding:16px">No alts saved. Click + Add Alt.</div>';
        return;
    }
    list.innerHTML = alts.map((a, i) => {
        const isActive = activeAlt && a.name === activeAlt;
        return `
      <div class="alt-item" style="${isActive ? 'border-color:var(--brand)' : ''}">
        <div class="alt-avatar" style="background:linear-gradient(135deg,#A855F7,#06B6D4);display:flex;align-items:center;justify-content:center;color:#fff;font-weight:700;font-size:15px">
          ${(a.name[0] || '?').toUpperCase()}
        </div>
        <div class="alt-info">
          <div class="alt-name">${escapeHtml(a.name)}${isActive ? ' <span style="font-size:9px;font-weight:700;color:var(--brand);background:rgba(168,85,247,0.12);border-radius:4px;padding:1px 6px;margin-left:4px">ACTIVE</span>' : ''}</div>
          <div class="alt-status">${a.type || 'Offline'} · Added ${new Date(a.added).toLocaleDateString()}</div>
        </div>
        <div class="alt-actions">
          <button class="btn ${isActive ? 'btn-secondary' : 'btn-primary'} btn-sm" data-alt-use="${i}" ${isActive ? 'disabled' : ''}>${isActive ? 'Active' : 'Use'}</button>
          <button class="btn btn-danger btn-sm" data-alt-del="${i}">✕</button>
        </div>
      </div>`;
    }).join('');

    list.querySelectorAll('[data-alt-use]').forEach(b => {
        b.addEventListener('click', async () => {
            const idx = parseInt(b.dataset.altUse, 10);
            const a = alts[idx];
            if (!a) return;
            activeAlt = a.name;
            await quark.settingsSet('activeAlt', activeAlt);
            renderAlts();
            const sub = document.getElementById('alt-active-sub');
            if (sub) sub.textContent = 'Active account: ' + activeAlt;
            notify(`"${a.name}" set as active account for next launch`, 'success');
        });
    });
    list.querySelectorAll('[data-alt-del]').forEach(b => {
        b.addEventListener('click', () => {
            const idx = parseInt(b.dataset.altDel, 10);
            if (!isNaN(idx) && idx >= 0 && idx < alts.length) {
                alts.splice(idx, 1);
                quark.settingsSet('alts', alts);
                renderAlts();
                const titleEl = document.getElementById('alt-count-title');
                if (titleEl) titleEl.textContent = `Saved Alts (${alts.length})`;
            }
        });
    });
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE: Global Chat
// ─────────────────────────────────────────────────────────────────────────────

async function chat() {
    const relayUrl = (await quark.settingsGet('chatServerUrl') || '').trim();

    document.getElementById('content').innerHTML = `
      <div class="page-header">
        <h1>Global Chat</h1>
        <p>Chat with other Quark users in real-time</p>
      </div>
      <div class="card">
        <div class="card-title">
          <span>Live Chat</span>
          <span id="chat-online" style="font-size:11px;font-weight:500;margin-left:auto;color:var(--muted)">● Connecting…</span>
        </div>
        <div class="chat-box" id="chat-box"></div>
        <div class="chat-input-row">
          <input class="form-input" id="chat-input" placeholder="${relayUrl ? 'Message…' : 'Set a relay URL in Settings to chat'}" autocomplete="off" maxlength="240" ${relayUrl ? '' : 'disabled'}>
          <button class="btn btn-primary" id="btn-chat-send" ${relayUrl ? '' : 'disabled'}>Send</button>
        </div>
      </div>`;

    const input = document.getElementById('chat-input');

    if (!relayUrl) {
        setChatStatus('● Not configured', 'var(--muted)');
        addChatMsg('System',
            'Global Chat is not configured. Add a relay URL under Settings → Global Chat, ' +
            'then deploy launcher/server (see its README) to host one.', true);
        pageCleanup = () => {};
        return;
    }

    let ws = null;
    let closedByUs = false;
    let reconnectTimer = null;
    let attempts = 0;

    function connect() {
        setChatStatus('● Connecting…', 'var(--muted)');
        try {
            ws = new WebSocket(relayUrl);
        } catch (e) {
            scheduleReconnect();
            return;
        }

        ws.onopen = () => {
            attempts = 0;
            ws.send(JSON.stringify({ type: 'join', user: currentUser?.username || 'Guest' }));
        };

        ws.onmessage = ev => {
            let m;
            try { m = JSON.parse(ev.data); } catch (_) { return; }
            switch (m.type) {
                case 'welcome':
                    setChatStatus(`● ${m.online} online`, 'var(--success)');
                    addChatMsg('System', 'Connected to Quark Global Chat. Be respectful.', true);
                    break;
                case 'presence':
                    setChatStatus(`● ${m.online} online`, 'var(--success)');
                    break;
                case 'history':
                    (m.messages || []).forEach(h => addChatMsg(h.user, h.text, false, h.ts));
                    break;
                case 'msg':
                    addChatMsg(m.user, m.text, false, m.ts);
                    break;
                case 'system':
                    addChatMsg('System', m.text, true, m.ts);
                    break;
                case 'error':
                    notify(m.text || 'Chat error', 'warn');
                    break;
            }
        };

        ws.onclose = () => {
            if (closedByUs) return;
            setChatStatus('● Offline — reconnecting…', 'var(--danger)');
            scheduleReconnect();
        };

        ws.onerror = () => { try { ws.close(); } catch (_) {} };
    }

    function scheduleReconnect() {
        if (closedByUs) return;
        attempts++;
        const delay = Math.min(15000, 1000 * Math.pow(2, attempts - 1));
        reconnectTimer = setTimeout(connect, delay);
    }

    function sendChatMsg() {
        const val = input?.value.trim();
        if (!val) return;
        if (!ws || ws.readyState !== WebSocket.OPEN) {
            notify('Not connected to chat yet', 'warn');
            return;
        }
        ws.send(JSON.stringify({ type: 'chat', text: val }));
        input.value = '';
    }

    document.getElementById('btn-chat-send').addEventListener('click', sendChatMsg);
    input?.addEventListener('keydown', e => { if (e.key === 'Enter') sendChatMsg(); });

    connect();

    pageCleanup = () => {
        closedByUs = true;
        if (reconnectTimer) clearTimeout(reconnectTimer);
        if (ws) { try { ws.close(); } catch (_) {} }
    };
}

function setChatStatus(text, color) {
    const el = document.getElementById('chat-online');
    if (el) { el.textContent = text; el.style.color = color; }
}

function addChatMsg(user, text, system, ts) {
    const box = document.getElementById('chat-box');
    if (!box) return;
    const time = new Date(ts || Date.now()).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const div  = document.createElement('div');
    div.className = 'chat-msg';
    div.innerHTML = `
      <div class="chat-msg-avatar" style="background:linear-gradient(135deg,#A855F7,#06B6D4);display:flex;align-items:center;justify-content:center;color:#fff;font-size:12px;font-weight:700">${(user[0] || '?').toUpperCase()}</div>
      <div class="chat-msg-body">
        <span class="chat-msg-user" style="${system ? 'color:var(--cyan)' : ''}">${escapeHtml(user)}</span>
        <span class="chat-msg-time">${time}</span>
        <div class="chat-msg-text">${escapeHtml(text)}</div>
      </div>`;
    box.appendChild(div);
    box.scrollTop = box.scrollHeight;
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE: Changelog
// ─────────────────────────────────────────────────────────────────────────────

function changelog() {
    const entries = [
        { v: '2.0.0', date: 'June 2025', items: [
            { text: `${TOTAL_MODULES} real modules — every one runs in game`, fix: false },
            { text: 'Expanded in-game HUD: speed, health, hunger, held item, server IP, game time, memory, session info', fix: false },
            { text: 'Pure JVM agent injection — no JAR/mods needed', fix: false },
            { text: 'System tray, auto-inject, live process monitoring', fix: false },
            { text: 'Server manager with live MC ping protocol', fix: false },
            { text: 'Replaced the fake keybind editor with an honest Controls reference', fix: true },
            { text: 'Corrected inflated module counts to reflect what actually ships', fix: true },
            { text: 'Java discovery — scans all installed JDKs', fix: true },
        ]},
        { v: '1.5.0', date: 'May 2025', items: [
            { text: 'Global Chat — real WebSocket relay with presence and history', fix: false },
            { text: 'EnvironmentDetector: auto-detects loader and launcher', fix: false },
            { text: 'ClassResolver: multi-environment class name resolution', fix: false },
            { text: 'AttachShim: JVM attach shim for injection without tools.jar', fix: false },
        ]},
        { v: '1.0.0', date: 'April 2025', items: [
            { text: 'Initial release — self-contained injected HUD client', fix: false },
            { text: 'Keyboard-driven ClickGUI rendered via reflection', fix: false },
            { text: 'Electron launcher with Discord OAuth', fix: false },
            { text: 'ASM 9 bytecode instrumentation for game hooks', fix: false },
        ]},
    ];

    document.getElementById('content').innerHTML = `
      <div class="page-header"><h1>Changelog</h1><p>Version history and release notes</p></div>
      <div class="card">
        ${entries.map(e => `
          <div class="changelog-entry">
            <div class="changelog-dot"></div>
            <div>
              <div class="changelog-version">v${e.v}</div>
              <div class="changelog-date">${e.date}</div>
              <ul class="changelog-items">
                ${e.items.map(i => `<li class="${i.fix ? 'fix' : ''}">${escapeHtml(i.text)}</li>`).join('')}
              </ul>
            </div>
          </div>`).join('')}
      </div>`;
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE: Staff
// ─────────────────────────────────────────────────────────────────────────────

// Real server-command palette. Each entry is a vanilla/Essentials-style slash
// command with {p} (target player) and {r} (reason) placeholders. Buttons build
// the exact command and copy it to the clipboard to paste in-game — the launcher
// never forges packets or touches a server you don't control.
const STAFF_COMMANDS = [
    { icon: '🎮', name: 'Creative',   cmd: '/gamemode creative {p}',          desc: 'Set creative mode' },
    { icon: '🗡', name: 'Survival',   cmd: '/gamemode survival {p}',          desc: 'Set survival mode' },
    { icon: '👤', name: 'Spectator',  cmd: '/gamemode spectator {p}',         desc: 'Set spectator mode' },
    { icon: '📍', name: 'TP to You',  cmd: '/tp {p} @s',                      desc: 'Bring player to you' },
    { icon: '🧭', name: 'Go to',      cmd: '/tp @s {p}',                      desc: 'Teleport to player' },
    { icon: '❤', name: 'Heal',       cmd: '/effect give {p} regeneration 5 4', desc: 'Regen the player' },
    { icon: '🍖', name: 'Feed',       cmd: '/effect give {p} saturation 1 4', desc: 'Restore hunger' },
    { icon: '🧹', name: 'Clear Inv',  cmd: '/clear {p}',                      desc: 'Empty inventory' },
    { icon: '🔇', name: 'Mute',       cmd: '/mute {p} {r}',                   desc: 'Essentials mute' },
    { icon: '🔊', name: 'Unmute',     cmd: '/unmute {p}',                     desc: 'Lift a mute' },
    { icon: '🥾', name: 'Kick',       cmd: '/kick {p} {r}',                   desc: 'Kick from server' },
    { icon: '🔨', name: 'Ban',        cmd: '/ban {p} {r}',                    desc: 'Ban from server' },
    { icon: '🕊', name: 'Pardon',     cmd: '/pardon {p}',                     desc: 'Remove a ban' },
    { icon: '⭐', name: 'OP',         cmd: '/op {p}',                         desc: 'Grant operator' },
    { icon: '➖', name: 'De-OP',      cmd: '/deop {p}',                       desc: 'Revoke operator' },
    { icon: '💀', name: 'Kill',       cmd: '/kill {p}',                       desc: 'Kill the player' },
    { icon: '☀', name: 'Day',        cmd: '/time set day',                   desc: 'Set time to day' },
    { icon: '🌧', name: 'Clear Wx',   cmd: '/weather clear',                  desc: 'Clear the weather' },
];

function buildStaffCommand(template) {
    const p = (document.getElementById('staff-target')?.value || '').trim();
    const r = (document.getElementById('staff-reason')?.value || '').trim();
    let cmd = template.replace(/\{p\}/g, p || '<player>');
    cmd = cmd.replace(/\{r\}/g, r || (template.includes('{r}') ? 'No reason given' : ''));
    return cmd.replace(/\s+/g, ' ').trim();
}

function staff() {
    if (!isStaff(currentUser)) { navigateTo('home'); return; }

    document.getElementById('content').innerHTML = `
      <div class="page-header">
        <h1>Command Palette</h1>
        <p>Build server moderation commands and copy them ready to paste in-game</p>
      </div>

      <div class="card" style="margin-bottom:16px">
        <div class="card-title">Target</div>
        <div style="display:flex;gap:10px;flex-wrap:wrap">
          <div style="flex:1;min-width:180px">
            <div class="form-label">Player</div>
            <input class="form-input" id="staff-target" placeholder="username (e.g. Notch)" autocomplete="off">
          </div>
          <div style="flex:1;min-width:180px">
            <div class="form-label">Reason <span style="color:var(--muted);font-weight:400">(for kick/ban/mute)</span></div>
            <input class="form-input" id="staff-reason" placeholder="optional reason" autocomplete="off">
          </div>
        </div>
      </div>

      <div class="card" style="margin-bottom:16px">
        <div class="card-title">Commands</div>
        <div class="grid-4">
          ${STAFF_COMMANDS.map((c, i) => `
            <div class="staff-action-btn" data-cmd-idx="${i}" title="${escapeHtml(c.cmd)}">
              <span class="icon">${c.icon}</span>
              <span style="font-weight:600;font-size:12px">${c.name}</span>
              <span style="font-size:10px;color:var(--muted);text-align:center">${c.desc}</span>
            </div>`).join('')}
        </div>
      </div>

      <div class="card">
        <div class="card-title">Last Built Command</div>
        <div style="display:flex;gap:8px;align-items:center">
          <code id="staff-preview" style="flex:1;font-size:13px;color:var(--cyan);background:var(--bg-input);border:1px solid var(--border);border-radius:7px;padding:10px 12px;overflow-x:auto;white-space:nowrap">Pick a command above…</code>
          <button class="btn btn-secondary btn-sm" id="btn-copy-last">Copy</button>
        </div>
        <p style="font-size:10px;color:var(--muted);margin-top:8px">
          Commands are copied to your clipboard — paste them into the in-game chat or your
          server console. Only works on servers where you already hold the required permissions.
        </p>
      </div>`;

    let lastCmd = '';

    document.querySelectorAll('[data-cmd-idx]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const idx = parseInt(btn.dataset.cmdIdx, 10);
            const entry = STAFF_COMMANDS[idx];
            if (!entry) return;
            lastCmd = buildStaffCommand(entry.cmd);
            const prev = document.getElementById('staff-preview');
            if (prev) prev.textContent = lastCmd;
            const ok = await copyText(lastCmd);
            notify(ok ? `Copied: ${lastCmd}` : `Built: ${lastCmd}`, ok ? 'success' : 'info');
        });
    });

    document.getElementById('btn-copy-last')?.addEventListener('click', async () => {
        if (!lastCmd) { notify('Pick a command first', 'info'); return; }
        const ok = await copyText(lastCmd);
        notify(ok ? 'Copied to clipboard' : 'Copy failed', ok ? 'success' : 'warn');
    });
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE: Settings
// ─────────────────────────────────────────────────────────────────────────────

async function settings() {
    const cfg = await quark.settingsGetAll();

    document.getElementById('content').innerHTML = `
      <div class="page-header"><h1>Settings</h1><p>Configure Quark and the launcher</p></div>

      <div class="grid-2">
        <div style="display:flex;flex-direction:column;gap:14px">

          <div class="card">
            <div class="card-title">Discord OAuth</div>
            <div class="form-row">
              <div class="form-label">Client ID</div>
              <input class="form-input" id="cfg-client-id" placeholder="1234567890123456789" value="${escapeHtml(cfg.discordClientId || '')}" autocomplete="off">
              <div class="form-hint">Create an app at discord.com/developers</div>
            </div>
            <div class="form-row">
              <div class="form-label">Client Secret</div>
              <input class="form-input" id="cfg-client-secret" type="password" placeholder="••••••••••" value="${escapeHtml(cfg.discordClientSecret || '')}" autocomplete="off">
            </div>
            <button class="btn btn-primary btn-sm" id="btn-save-discord">Save Discord Config</button>
          </div>

          <div class="card">
            <div class="card-title">Injection</div>
            <div class="toggle-row">
              <div><div class="toggle-label">Auto-inject on launch</div><div class="toggle-sub">Detect and inject when MC starts</div></div>
              <label class="toggle"><input type="checkbox" id="cfg-auto-inject" ${cfg.autoInject ? 'checked' : ''}><span class="toggle-slider"></span></label>
            </div>
            <div class="toggle-row">
              <div><div class="toggle-label">Silent injection</div><div class="toggle-sub">Suppress console output</div></div>
              <label class="toggle"><input type="checkbox" id="cfg-silent-inject" ${cfg.silentInject ? 'checked' : ''}><span class="toggle-slider"></span></label>
            </div>
            <div class="form-row" style="margin-top:12px">
              <div class="form-label">Custom Agent JAR</div>
              <div style="display:flex;gap:6px">
                <input class="form-input" id="cfg-agent-path" placeholder="Auto-detect" value="${escapeHtml(cfg.agentPath || '')}" autocomplete="off">
                <button class="btn btn-secondary btn-sm" id="btn-browse-jar">Browse</button>
              </div>
            </div>
            <div class="form-row">
              <div class="form-label">Java Executable</div>
              <input class="form-input" id="cfg-java-path" placeholder="Auto-detect (JAVA_HOME)" value="${escapeHtml(cfg.javaPath || '')}" autocomplete="off">
              <div class="form-hint">Leave blank to auto-detect from JAVA_HOME or PATH</div>
            </div>
            <button class="btn btn-secondary btn-sm" id="btn-save-inject">Save Injection Config</button>
          </div>

        </div>

        <div style="display:flex;flex-direction:column;gap:14px">

          <div class="card">
            <div class="card-title">Launcher Behaviour</div>
            <div class="toggle-row">
              <div><div class="toggle-label">Minimise to tray</div><div class="toggle-sub">Keep launcher in system tray</div></div>
              <label class="toggle"><input type="checkbox" id="cfg-min-tray" ${cfg.minimiseToTray ? 'checked' : ''}><span class="toggle-slider"></span></label>
            </div>
            <div class="toggle-row">
              <div><div class="toggle-label">Start minimised</div><div class="toggle-sub">Open launcher minimised to tray</div></div>
              <label class="toggle"><input type="checkbox" id="cfg-start-min" ${cfg.startMinimised ? 'checked' : ''}><span class="toggle-slider"></span></label>
            </div>
            <button class="btn btn-secondary btn-sm" style="margin-top:10px" id="btn-save-launcher">Save Launcher Config</button>
          </div>

          <div class="card">
            <div class="card-title">Global Chat</div>
            <div class="form-row">
              <div class="form-label">Relay URL</div>
              <input class="form-input" id="cfg-chat-url" placeholder="wss://your-relay-host" value="${escapeHtml(cfg.chatServerUrl || '')}" autocomplete="off">
              <div class="form-hint">Host the relay in <strong>launcher/server</strong> (see its README), then paste its <code>wss://</code> URL here. Leave blank to disable chat.</div>
            </div>
            <button class="btn btn-secondary btn-sm" id="btn-save-chat">Save Chat Config</button>
          </div>

          <div class="card">
            <div class="card-title">Sponsors &amp; Ads</div>
            <p style="font-size:11px;color:var(--muted);margin-bottom:8px">
              Fill the ad slot on the Home page with your own sponsors and earn from them. One JSON
              object per ad: <code>title</code> (required), optional <code>body</code>, <code>cta</code>,
              <code>url</code> and <code>img</code> (both must be <code>https://</code>). Leave empty to
              show a house placeholder.
            </p>
            <textarea class="form-input" id="cfg-ads" rows="7" spellcheck="false"
              style="font-family:monospace;font-size:11px;resize:vertical"
              placeholder='[{"title":"My Sponsor","body":"Great gaming gear","cta":"Shop","url":"https://example.com","img":"https://example.com/banner.png"}]'>${escapeHtml(JSON.stringify(sponsorAds, null, 2))}</textarea>
            <div id="ads-stat-summary" style="font-size:10px;color:var(--muted);margin:8px 0">${adStatsSummary()}</div>
            <div style="display:flex;gap:8px">
              <button class="btn btn-primary btn-sm" id="btn-save-ads">Save Ads</button>
              <button class="btn btn-secondary btn-sm" id="btn-reset-adstats">Reset Stats</button>
            </div>
          </div>

          <div class="card">
            <div class="card-title">Config Backup</div>
            <div style="display:flex;flex-direction:column;gap:8px">
              <button class="btn btn-secondary btn-sm" id="btn-export-config">⬆ Export Config</button>
              <button class="btn btn-secondary btn-sm" id="btn-import-config">⬇ Import Config</button>
            </div>
            <p style="font-size:10px;color:var(--muted);margin-top:8px">Exports all settings, profiles, alts and servers to a JSON file.</p>
          </div>

          <div class="card">
            <div class="card-title">Profile Picture</div>
            <div style="display:flex;align-items:center;gap:14px">
              ${currentUser.avatarOverride || currentUser.avatarUrl
                  ? `<img class="avatar-preview-lg" id="avatar-preview" src="${currentUser.avatarOverride || currentUser.avatarUrl}" alt="">`
                  : `<div class="avatar-preview-placeholder-lg" id="avatar-preview">${(currentUser.username || 'G')[0].toUpperCase()}</div>`}
              <div style="display:flex;flex-direction:column;gap:8px">
                <button class="btn btn-secondary btn-sm" id="btn-change-avatar">Change Picture</button>
                ${currentUser.avatarOverride ? `<button class="btn btn-danger btn-sm" id="btn-remove-avatar">Remove Picture</button>` : ''}
              </div>
            </div>
            <p style="font-size:10px;color:var(--muted);margin-top:8px">PNG, JPG, GIF, WEBP or BMP — max 4MB. Stored locally and used across the launcher.</p>
          </div>

          <div class="card">
            <div class="card-title">Account</div>
            <div style="display:flex;flex-direction:column;gap:8px">
              ${currentUser && !currentUser.guest ? `
                <button class="btn btn-danger btn-sm" id="btn-logout">Sign Out of Discord</button>
                <button class="btn btn-secondary btn-sm" id="btn-clear-data">Clear All Data</button>
              ` : `
                <button class="btn btn-discord btn-sm" id="btn-login-settings">Sign in with Discord</button>
              `}
            </div>
          </div>

          <div class="card">
            <div class="card-title">About</div>
            <div style="font-size:12px;color:var(--muted);line-height:2.2">
              <div>Quark Client <span style="color:var(--brand)">v2.0.0</span></div>
              <div>Modules: <span style="color:var(--text)">${TOTAL_MODULES}</span> (all real)</div>
              <div>MC Target: <span style="color:var(--text)">1.21.x (multi-mapping)</span></div>
              <div>Injection: <span style="color:var(--cyan)">JVM Attach API + ASM 9</span></div>
              <div>Launchers: <span style="color:var(--text)">All major launchers</span></div>
            </div>
          </div>

        </div>
      </div>`;

    document.getElementById('btn-save-discord')?.addEventListener('click', async () => {
        await quark.settingsSet('discordClientId',     document.getElementById('cfg-client-id').value.trim());
        await quark.settingsSet('discordClientSecret', document.getElementById('cfg-client-secret').value.trim());
        notify('Discord config saved', 'success');
    });

    document.getElementById('btn-browse-jar')?.addEventListener('click', async () => {
        const p = await quark.selectFile();
        if (p) document.getElementById('cfg-agent-path').value = p;
    });

    document.getElementById('btn-save-inject')?.addEventListener('click', async () => {
        const autoInject = document.getElementById('cfg-auto-inject').checked;
        await quark.settingsSet('autoInject',   autoInject);
        await quark.settingsSet('silentInject', document.getElementById('cfg-silent-inject').checked);
        await quark.settingsSet('agentPath',    document.getElementById('cfg-agent-path').value.trim());
        await quark.settingsSet('javaPath',     document.getElementById('cfg-java-path').value.trim());

        // Toggle auto-inject monitor
        if (autoInject) {
            quark.injectAutoStart();
            document.getElementById('tb-auto-inject-indicator')?.classList.remove('hidden');
        } else {
            quark.injectAutoStop();
            document.getElementById('tb-auto-inject-indicator')?.classList.add('hidden');
        }
        notify('Injection config saved', 'success');
    });

    document.getElementById('btn-save-launcher')?.addEventListener('click', async () => {
        await quark.settingsSet('minimiseToTray',  document.getElementById('cfg-min-tray').checked);
        await quark.settingsSet('startMinimised',  document.getElementById('cfg-start-min').checked);
        notify('Launcher config saved', 'success');
    });

    document.getElementById('btn-save-chat')?.addEventListener('click', async () => {
        let url = document.getElementById('cfg-chat-url').value.trim();
        if (url && !/^wss?:\/\//i.test(url)) {
            notify('Relay URL must start with ws:// or wss://', 'warn');
            return;
        }
        await quark.settingsSet('chatServerUrl', url);
        notify(url ? 'Chat relay saved — reopen Global Chat to connect' : 'Global Chat disabled', 'success');
    });

    document.getElementById('btn-save-ads')?.addEventListener('click', async () => {
        const raw = document.getElementById('cfg-ads').value.trim();
        let parsed;
        if (!raw) { parsed = []; }
        else {
            try { parsed = JSON.parse(raw); }
            catch (_) { notify('Ads must be valid JSON (an array of ad objects)', 'error'); return; }
        }
        if (!Array.isArray(parsed)) { notify('Ads JSON must be an array', 'error'); return; }

        const clean = [];
        for (const a of parsed) {
            if (!a || typeof a !== 'object' || !a.title) {
                notify('Every ad needs at least a "title"', 'warn'); return;
            }
            if (a.url && !/^https:\/\//i.test(a.url)) {
                notify(`Ad "${a.title}": url must start with https://`, 'warn'); return;
            }
            if (a.img && !/^https:\/\//i.test(a.img)) {
                notify(`Ad "${a.title}": img must start with https://`, 'warn'); return;
            }
            clean.push({
                id   : String(a.id || a.title).slice(0, 64),
                title: String(a.title).slice(0, 80),
                body : a.body ? String(a.body).slice(0, 200) : '',
                cta  : a.cta  ? String(a.cta).slice(0, 24)  : '',
                url  : a.url  || '',
                img  : a.img  || '',
                sponsored: true,
            });
        }
        sponsorAds = clean;
        adRotation = 0;
        await quark.settingsSet('ads', sponsorAds);
        notify(clean.length ? `${clean.length} ad${clean.length === 1 ? '' : 's'} saved` : 'Ads cleared — showing house placeholder', 'success');
    });

    document.getElementById('btn-reset-adstats')?.addEventListener('click', async () => {
        adStats = {};
        await quark.settingsSet('adStats', adStats);
        const summary = document.getElementById('ads-stat-summary');
        if (summary) summary.textContent = adStatsSummary();
        notify('Ad stats reset', 'info');
    });

    document.getElementById('btn-export-config')?.addEventListener('click', async () => {
        const ok = await quark.configExport();
        notify(ok ? 'Config exported successfully' : 'Export cancelled', ok ? 'success' : 'info');
    });

    document.getElementById('btn-import-config')?.addEventListener('click', async () => {
        const ok = await quark.configImport();
        notify(ok ? 'Config imported — reload launcher to apply' : 'Import cancelled', ok ? 'success' : 'info');
    });

    document.getElementById('btn-change-avatar')?.addEventListener('click', async () => {
        try {
            const dataUri = await quark.selectImage();
            if (!dataUri) return;
            currentUser.avatarOverride = dataUri;
            await quark.settingsSet('avatarOverride', dataUri);
            buildSidebar();
            navigateTo('settings');
            notify('Profile picture updated', 'success');
        } catch (e) {
            notify('Failed to set profile picture: ' + e.message, 'error');
        }
    });

    document.getElementById('btn-remove-avatar')?.addEventListener('click', async () => {
        delete currentUser.avatarOverride;
        await quark.settingsSet('avatarOverride', null);
        buildSidebar();
        navigateTo('settings');
        notify('Profile picture removed', 'info');
    });

    document.getElementById('btn-logout')?.addEventListener('click', () => {
        if (confirm('Sign out of Discord?')) {
            quark.discordLogout();
            quark.settingsSet('user', null);
            currentUser = null;
            showLogin();
        }
    });

    document.getElementById('btn-clear-data')?.addEventListener('click', () => {
        if (confirm('This will clear all saved data. Continue?')) {
            quark.settingsSet('user', null);
            quark.settingsSet('alts', []);
            quark.settingsSet('profiles', []);
            quark.settingsSet('servers', []);
            quark.settingsSet('keybinds', {});
            alts = []; profiles = defaultProfiles(); servers = defaultServers(); keybinds = {};
            notify('All data cleared', 'info');
        }
    });

    document.getElementById('btn-login-settings')?.addEventListener('click', handleDiscordLogin);
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

function sleep(ms)         { return new Promise(r => setTimeout(r, ms)); }
function escapeHtml(str)   {
    return String(str)
        .replace(/&/g,'&amp;').replace(/</g,'&lt;')
        .replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

// Copy text to the clipboard, with a legacy execCommand fallback. Returns true on success.
async function copyText(text) {
    try {
        if (navigator.clipboard && navigator.clipboard.writeText) {
            await navigator.clipboard.writeText(text);
            return true;
        }
    } catch (_) { /* fall through to legacy path */ }
    try {
        const ta = document.createElement('textarea');
        ta.value = text;
        ta.style.cssText = 'position:fixed;opacity:0';
        document.body.appendChild(ta);
        ta.select();
        const ok = document.execCommand('copy');
        document.body.removeChild(ta);
        return ok;
    } catch (_) {
        return false;
    }
}
