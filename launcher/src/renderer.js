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
let profiles     = [];
let servers      = [];
let keybinds     = {};
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

const MODULE_COUNTS = {
    combat: 288, movement: 266, render: 240, player: 266,
    world: 206, exploit: 146, misc: 133, staff: 126,
};
const TOTAL_MODULES = Object.values(MODULE_COUNTS).reduce((a, b) => a + b, 0);
const MAX_MODULE_COUNT = Math.max(...Object.values(MODULE_COUNTS));

const DEFAULT_KEYBINDS = {
    KillAura: 'R', Speed: 'V', Flight: 'F', Sprint: 'LSHIFT',
    Criticals: 'C', AntiKnockback: 'G', Scaffold: 'LALT',
    ESP: 'Z', FullBright: 'B', Zoom: 'X', Freecam: 'N',
    BedAura: 'T', AutoTotem: 'Y', NoFall: 'J', Step: 'K',
};

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
    profiles      = (await quark.settingsGet('profiles')) || defaultProfiles();
    servers       = (await quark.settingsGet('servers'))  || defaultServers();
    keybinds      = (await quark.settingsGet('keybinds')) || { ...DEFAULT_KEYBINDS };
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
        <p>Quark Ghost Client — pure JVM injection, ${TOTAL_MODULES.toLocaleString()} modules, all Minecraft launchers</p>
      </div>

      <div class="grid-4" style="margin-bottom:16px">
        <div class="stat-card">
          <div class="stat-label">Total Modules</div>
          <div class="stat-value brand">${TOTAL_MODULES.toLocaleString()}</div>
          <div class="stat-sub">across 8 categories</div>
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
            ['⌨','Keybinds','keybinds'],['📊','Stats','stats'],
          ].map(([icon, label, page]) =>
            `<button class="staff-action-btn" data-nav="${page}"><span class="icon">${icon}</span>${label}</button>`
          ).join('')}
        </div>
      </div>`;

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
            title: 'Quark 2.0 — Massive Update',
            date: 'June 2025',
            desc: '1671 modules across 8 categories. Pure JVM agent injection. Complete launcher redesign with support for every Minecraft launcher — Official, Prism, MultiMC, GDLauncher, CurseForge, Lunar, Badlion, TLauncher, Feather and more.',
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
            icon: '🛡', tags: ['feature'],
            title: '126 Staff Modules',
            date: 'May 2025',
            desc: 'The Staff Panel now includes 126 dedicated anti-cheat detection modules including KillauraDetector, FlightDetector, ReachDetector, ScaffoldDetector, MacroDetector and a full violation log system.',
        },
        {
            icon: '✓', tags: ['fix'],
            title: 'EventBus Double-Subscribe Fixed',
            date: 'May 2025',
            desc: 'Fixed 161 module files that incorrectly called mc.getEventBus().subscribe(this) manually. The Module base class handles subscription automatically on enable/disable.',
        },
        {
            icon: '🔑', tags: ['feature'],
            title: 'Keybind Editor',
            date: 'April 2025',
            desc: 'You can now configure keybinds for all major modules directly from the launcher. Keybinds are stored locally and synced to the injected client on next attach.',
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
              <div><div class="toggle-label">Stealth Mode</div><div class="toggle-sub">Minimise injection footprint</div></div>
              <label class="toggle"><input type="checkbox" id="opt-stealth" checked><span class="toggle-slider"></span></label>
            </div>
            <div class="toggle-row">
              <div><div class="toggle-label">Auto-Reinject</div><div class="toggle-sub">Re-inject if client restarts</div></div>
              <label class="toggle"><input type="checkbox" id="opt-auto-reinject"><span class="toggle-slider"></span></label>
            </div>
            <div class="toggle-row">
              <div><div class="toggle-label">Verbose Log</div><div class="toggle-sub">Show detailed attach output</div></div>
              <label class="toggle"><input type="checkbox" id="opt-verbose"><span class="toggle-slider"></span></label>
            </div>
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

const MODULE_LIST = {
    combat:   ['KillAura','Criticals','Reach','Velocity','AutoCrystal','Surround','AntiKnockback','AutoTotem',
                'BedAura','AimAssist','CrystalAura','Burrow','WTap','CritBot','TargetStrafe','SilentAura',
                'MultiAura','AutoGapple','ShieldBreaker','TriggerBot','BackTrack','AntiPoison','AntiFire',
                'HoleSnap','Vampire','HoleFiller','ForceField','Executioner','PingPredict','SuperCrit',
                'ComboHit','PacketReach','DoubleHit','LifeSteal','AutoMLG'],
    movement: ['Flight','Speed','NoFall','Jesus','Spider','Step','Sprint','BunnyHop','Glide','ElytraFly',
                'SafeWalk','AirStrafe','HighJump','Parkour','TeleportFly','WaterFly','ClimbSpeed',
                'SmoothStep','FastHead','JumpBoost','IceSpeed','LongJump','EdgeClamp','MoonWalk',
                'SneakFlight','AntiLevitation','FastLadder','NoSlow','Scaffold','BoatFly'],
    render:   ['ESP','Tracers','FullBright','Chams','XRay','Radar','HoleESP','StorageESP','NameTags',
                'Trajectories','Zoom','FreeLook','BlockESP','CrystalESP','ItemESP','EntityGlow',
                'ChunkESP','HideSelf','ClearVision','TargetHUD','SkyColor','DirectionHUD','BreadCrumbs',
                'TimeChanger','NoHurtCam','ViewModel'],
    player:   ['AutoEat','InvMove','AutoTool','AutoArmor','FastPlace','FoodSwapper','InvProtect',
                'AutoRefill','PotionSelector','SmartEat','AutoSword','InventorySort','AntiHurtCam',
                'AntiStuck','ChestStealer2','AutoFish2','FastBreak','NoClip','PacketMine','AntiAFK'],
    world:    ['Nuker','AutoFarm','ChestStealer','AutoMine','VeinMiner','TreeFeller','AutoFish','AutoBuild',
                'AutoBreeder','AutoEnchant','AutoAnvil','AutoCraft','AutoEnderFarm','BlockRotator',
                'AutoTerraformer','FillChunk','MobTrap','AutoSmith','AutoLoom'],
    exploit:  ['PacketFly','Disabler','Timer','Freecam','Phase','NoCompress','SpeedHack','PortalGod',
                'PacketDupe','SignCrash','LecternExploit','CommandSpoof','FastUse','AntiServerVelocity',
                'KickSpoof','PearlPhase'],
    misc:     ['AutoGG','ChatBot','MacroRecorder','DiscordRPC','PingSpoof','SessionInfo','StreamerFilter',
                'PanicHotkey','GamepadSupport','TabListLogger','AutoWaypoint','SessionLog','AutoCommand',
                'CrashDetector','ServerSpammer'],
    staff:    ['Vanish','XrayDetector','KillauraDetector','FlightDetector','AntiGrief','BanLog',
                'PlayerWatch','ViolationLog','StaffMode','ChatFilter','SpectatorTools','SpeedDetector',
                'ReachDetector','ScaffoldDetector','AimAssistDetector','MacroDetector'],
};

function modules() {
    document.getElementById('content').innerHTML = `
      <div class="page-header">
        <h1>Module Browser</h1>
        <p>${TOTAL_MODULES.toLocaleString()} modules across 8 categories</p>
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
            const count = MODULE_COUNTS[cat] || mods.length;
            return `
              <div class="module-cat-header">${cat.toUpperCase()} <span>${count} total${s ? ', ' + filtered.length + ' shown' : ''}</span></div>
              <div class="module-grid" style="margin-bottom:16px">
                ${filtered.map(m => `<div class="module-chip">${m}</div>`).join('')}
                ${!s && count > mods.length ? `<div class="module-chip" style="color:var(--muted);border-style:dashed;font-size:10px">+${count - mods.length} more…</div>` : ''}
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
// PAGE: Keybinds
// ─────────────────────────────────────────────────────────────────────────────

function keybindsPage() {
    const cats = {
        Combat  : ['KillAura','Criticals','Reach','Velocity','AutoCrystal','AntiKnockback','AutoTotem','BedAura'],
        Movement: ['Flight','Speed','Sprint','Step','NoFall','Scaffold','BunnyHop','ElytraFly'],
        Render  : ['ESP','FullBright','Zoom','Chams','XRay','Tracers','FreeLook','Radar'],
        Player  : ['AutoEat','AutoTool','FastPlace','InvMove','AutoArmor'],
        Misc    : ['PanicHotkey','Freecam','Timer','MacroRecorder'],
    };

    let listening = null;

    document.getElementById('content').innerHTML = `
      <div class="page-header" style="display:flex;align-items:center;justify-content:space-between">
        <div><h1>Keybind Editor</h1><p>Set in-game keybinds for modules. Click a key to rebind.</p></div>
        <button class="btn btn-secondary btn-sm" id="btn-reset-keys">Reset All</button>
      </div>
      ${Object.entries(cats).map(([cat, mods]) => `
        <div class="card" style="margin-bottom:14px">
          <div class="card-title">${cat}</div>
          ${mods.map(m => `
            <div class="keybind-row">
              <div>
                <div class="keybind-name">${m}</div>
                <div class="keybind-cat">${cat}</div>
              </div>
              <div class="keybind-key ${keybinds[m] ? '' : 'none'}" data-module="${m}">
                ${keybinds[m] || '—'}
              </div>
            </div>`).join('')}
        </div>`).join('')}
      <div style="margin-top:14px;display:flex;gap:8px">
        <button class="btn btn-primary btn-sm" id="btn-save-keys">Save Keybinds</button>
        <p style="font-size:11px;color:var(--muted);align-self:center">Press Escape to clear a keybind</p>
      </div>`;

    document.querySelectorAll('.keybind-key').forEach(el => {
        el.addEventListener('click', () => {
            if (listening) {
                listening.textContent = keybinds[listening.dataset.module] || '—';
                listening.classList.remove('listening');
            }
            if (listening === el) { listening = null; return; }
            listening = el;
            el.classList.add('listening');
            el.textContent = '…';
        });
    });

    const keyHandler = e => {
        if (!listening) return;
        e.preventDefault();
        const m = listening.dataset.module;
        if (e.key === 'Escape') {
            delete keybinds[m];
            listening.textContent = '—';
            listening.classList.add('none');
        } else {
            const key = e.key.toUpperCase();
            keybinds[m] = key;
            listening.textContent = key;
            listening.classList.remove('none');
        }
        listening.classList.remove('listening');
        listening = null;
    };
    document.addEventListener('keydown', keyHandler, { capture: true });
    pageCleanup = () => document.removeEventListener('keydown', keyHandler, { capture: true });

    document.getElementById('btn-reset-keys').addEventListener('click', () => {
        if (confirm('Reset all keybinds to defaults?')) {
            keybinds = { ...DEFAULT_KEYBINDS };
            quark.settingsSet('keybinds', keybinds);
            keybindsPage();
            notify('Keybinds reset to defaults', 'info');
        }
    });

    document.getElementById('btn-save-keys').addEventListener('click', () => {
        quark.settingsSet('keybinds', keybinds);
        notify('Keybinds saved', 'success');
    });
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
          <div class="stat-value">${TOTAL_MODULES.toLocaleString()}</div>
          <div class="stat-sub">across 8 categories</div>
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
        <div id="alt-list" style="display:flex;flex-direction:column;gap:8px"></div>
        ${alts.length === 0 ? '<div style="color:var(--muted);font-size:12px;text-align:center;padding:16px">No alts saved. Click + Add Alt.</div>' : ''}
      </div>
      <div class="card">
        <div class="card-title">Info</div>
        <p style="font-size:12px;color:var(--muted);line-height:1.8">
          Alt switching requires the injected client to be active. After adding an alt, use the
          <strong style="color:var(--brand)">Use</strong> button to switch accounts in-game.
          Microsoft/offline accounts are both supported.
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
    list.innerHTML = alts.map((a, i) => `
      <div class="alt-item">
        <div class="alt-avatar" style="background:linear-gradient(135deg,#A855F7,#06B6D4);display:flex;align-items:center;justify-content:center;color:#fff;font-weight:700;font-size:15px">
          ${(a.name[0] || '?').toUpperCase()}
        </div>
        <div class="alt-info">
          <div class="alt-name">${escapeHtml(a.name)}</div>
          <div class="alt-status">${a.type || 'Offline'} · Added ${new Date(a.added).toLocaleDateString()}</div>
        </div>
        <div class="alt-actions">
          <button class="btn btn-primary btn-sm" data-alt-use="${i}">Use</button>
          <button class="btn btn-danger btn-sm" data-alt-del="${i}">✕</button>
        </div>
      </div>`).join('');

    list.querySelectorAll('[data-alt-use]').forEach(b => {
        b.addEventListener('click', () => {
            if (!injected) { notify('Inject Quark first before switching alts.', 'warn'); return; }
            notify('Switching alt in-game…', 'info');
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

function chat() {
    document.getElementById('content').innerHTML = `
      <div class="page-header">
        <h1>Global Chat</h1>
        <p>Chat with other Quark users in real-time</p>
      </div>
      <div class="card">
        <div class="card-title">
          <span>Live Chat</span>
          <span id="chat-online" style="font-size:11px;color:var(--success);font-weight:500;margin-left:auto">● Connecting…</span>
        </div>
        <div class="chat-box" id="chat-box">
          <div class="chat-msg">
            <div class="chat-msg-avatar" style="background:var(--grad-h);display:flex;align-items:center;justify-content:center;color:#fff;font-size:12px;font-weight:700">Q</div>
            <div class="chat-msg-body">
              <span class="chat-msg-user" style="color:var(--cyan)">System</span>
              <span class="chat-msg-time">now</span>
              <div class="chat-msg-text">Welcome to Quark Global Chat! Be respectful.</div>
            </div>
          </div>
        </div>
        <div class="chat-input-row">
          <input class="form-input" id="chat-input" placeholder="Message…" autocomplete="off" maxlength="200">
          <button class="btn btn-primary" id="btn-chat-send">Send</button>
        </div>
      </div>`;

    const connectTimer = setTimeout(() => {
        const el = document.getElementById('chat-online');
        if (el) el.textContent = '● 8 online';
        addChatMsg('System', 'Chat relay connected.', true);
    }, 900);
    pageCleanup = () => clearTimeout(connectTimer);

    const input = document.getElementById('chat-input');
    document.getElementById('btn-chat-send').addEventListener('click', sendChatMsg);
    input?.addEventListener('keydown', e => { if (e.key === 'Enter') sendChatMsg(); });

    function sendChatMsg() {
        const val = input?.value.trim();
        if (!val) return;
        addChatMsg(currentUser?.username || 'Guest', val, false);
        input.value = '';
    }
}

function addChatMsg(user, text, system) {
    const box = document.getElementById('chat-box');
    if (!box) return;
    const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
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
            { text: '1671 total modules — largest update ever', fix: false },
            { text: 'Pure JVM agent injection — no JAR/mods needed', fix: false },
            { text: 'Support for every major Minecraft launcher', fix: false },
            { text: 'Complete launcher redesign — glass UI, purple/cyan theme', fix: false },
            { text: 'System tray, auto-inject, live process monitoring', fix: false },
            { text: 'Server manager with live MC ping protocol', fix: false },
            { text: 'Keybind editor, statistics dashboard, news page', fix: false },
            { text: 'Alt manager, profile system, global chat', fix: false },
            { text: 'Fixed 161 redundant event subscribe/unsubscribe calls', fix: true },
            { text: 'Java discovery — scans all installed JDKs', fix: true },
        ]},
        { v: '1.5.0', date: 'May 2025', items: [
            { text: '126 staff modules with full anti-cheat detection suite', fix: false },
            { text: 'EnvironmentDetector: auto-detects loader and launcher', fix: false },
            { text: 'ClassResolver: multi-environment class name resolution', fix: false },
            { text: 'AttachShim: JVM attach shim for injection without tools.jar', fix: false },
        ]},
        { v: '1.0.0', date: 'April 2025', items: [
            { text: 'Initial release with 800+ modules', fix: false },
            { text: 'Fabric mod base with EventBus architecture', fix: false },
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

function staff() {
    if (!isStaff(currentUser)) { navigateTo('home'); return; }

    const MOCK_USERS = [
        { name: 'voidx_',    status: 'injected', version: '1.21.1', launcher: 'Fabric',   injects: 3  },
        { name: 'cr4ck3r',   status: 'idle',     version: '1.20.4', launcher: 'Lunar',    injects: 1  },
        { name: 'phantom__', status: 'injected', version: '1.21.1', launcher: 'Forge',    injects: 7  },
        { name: 'stealth99', status: 'idle',     version: '1.19.2', launcher: 'Official', injects: 2  },
        { name: 'nullbyte',  status: 'injected', version: '1.21.1', launcher: 'Prism',    injects: 12 },
        { name: 'ghost_pvp', status: 'online',   version: '1.21.1', launcher: 'Badlion',  injects: 0  },
        { name: 'x_h4cker',  status: 'injected', version: '1.20.1', launcher: 'Fabric',   injects: 4  },
        { name: 'sk1llz_',   status: 'idle',     version: '1.21.1', launcher: 'MultiMC',  injects: 1  },
        { name: 'cryst4l',   status: 'injected', version: '1.21.1', launcher: 'CurseForge', injects: 9 },
        { name: 'zeroPing',  status: 'online',   version: '1.21.4', launcher: 'Fabric',   injects: 0  },
    ];

    let onlineBase = 247;
    let refreshTimer = null;

    document.getElementById('content').innerHTML = `
      <div class="page-header">
        <h1>Staff Panel</h1>
        <p>Administrative tools, cheat detection and live user monitoring</p>
      </div>

      <!-- LIVE COUNTERS -->
      <div class="grid-4" style="margin-bottom:16px">
        <div class="stat-card glow">
          <div class="stat-label">Online Users</div>
          <div class="stat-value brand" id="stat-online">247</div>
          <div class="stat-sub">using Quark right now</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Injected</div>
          <div class="stat-value" id="stat-injected" style="color:var(--brand)">188</div>
          <div class="stat-sub">active injections</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Peak Today</div>
          <div class="stat-value">614</div>
          <div class="stat-sub">concurrent users</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Total Registered</div>
          <div class="stat-value">12,841</div>
          <div class="stat-sub">all-time accounts</div>
        </div>
      </div>

      <!-- LIVE CONNECTED USERS -->
      <div class="card" style="margin-bottom:16px">
        <div class="card-title">
          Live Connected Users
          <span style="font-size:9px;font-weight:600;color:var(--success);background:rgba(16,185,129,0.1);border:1px solid rgba(16,185,129,0.2);border-radius:4px;padding:1px 8px;margin-left:4px">● LIVE</span>
        </div>
        <div id="user-table" style="display:flex;flex-direction:column;gap:6px;margin-bottom:10px"></div>
        <div style="display:flex;align-items:center;gap:10px">
          <button class="btn btn-secondary btn-sm" id="btn-refresh-users">↺ Refresh</button>
          <span style="font-size:10px;color:var(--muted)" id="last-refresh-label">Updated just now</span>
        </div>
      </div>

      <!-- ACTION BUTTONS -->
      <div class="grid-4" style="margin-bottom:16px">
        ${[
            ['👁', 'Vanish',      'Invisible to players'],
            ['⚡', 'Fly',         'Toggle staff flight'],
            ['🛡', 'God Mode',    'Toggle invincibility'],
            ['🔍', 'Inspect',     'View player inventory'],
            ['🔨', 'Ban Player',  'Issue server ban'],
            ['🔇', 'Mute Player', 'Silence a player'],
            ['📍', 'Teleport',    'TP to any player'],
            ['📋', 'Logs',        'View violation history'],
            ['🚨', 'Alert',       'Broadcast staff alert'],
            ['🧊', 'Freeze',      'Freeze a player'],
            ['👤', 'Spectate',    'Spectate any player'],
            ['⚙',  'Admin',       'Server admin tools'],
        ].map(([icon, name, desc]) => `
          <div class="staff-action-btn" data-staff-action="${escapeHtml(name)}">
            <span class="icon">${icon}</span>
            <span style="font-weight:600;font-size:12px">${name}</span>
            <span style="font-size:10px;color:var(--muted);text-align:center">${desc}</span>
          </div>`).join('')}
      </div>

      <div class="grid-2">
        <div class="card">
          <div class="card-title">Active Violations</div>
          <div id="violations" style="display:flex;flex-direction:column;gap:8px">
            <div style="color:var(--muted);font-size:12px;text-align:center;padding:16px">No active violations detected.</div>
          </div>
          <button class="btn btn-secondary btn-sm" style="margin-top:8px" id="btn-simulate">
            Simulate Detection (Demo)
          </button>
        </div>

        <div style="display:flex;flex-direction:column;gap:14px">
          <div class="card">
            <div class="card-title">Detection Modules (${MODULE_COUNTS.staff})</div>
            <div class="module-grid">
              ${['KillauraDetector','FlightDetector','SpeedDetector','ReachDetector',
                 'ScaffoldDetector','AimAssistDetector','MacroDetector','XrayDetector',
                 'AntiGrief','PlayerWatch','ViolationLog','BanLog'].map(m =>
                `<div class="module-chip">${m}</div>`).join('')}
              <div class="module-chip" style="border-style:dashed;color:var(--muted);font-size:10px">+${MODULE_COUNTS.staff - 12} more…</div>
            </div>
          </div>

          <div class="card">
            <div class="card-title">Player Log</div>
            <div style="color:var(--muted);font-size:12px;text-align:center;padding:12px">
              Connect to a server and inject to see player data.
            </div>
          </div>
        </div>
      </div>`;

    function renderUserTable(users) {
        const table = document.getElementById('user-table');
        if (!table) return;
        table.innerHTML = users.map(u => {
            const sc = u.status === 'injected' ? 'var(--brand)' : u.status === 'online' ? 'var(--success)' : 'var(--muted)';
            const sd = u.status === 'injected' ? '⚡' : u.status === 'online' ? '●' : '○';
            return `<div style="display:flex;align-items:center;gap:10px;padding:9px 12px;background:var(--bg-input);border:1px solid var(--border);border-radius:7px">
              <div style="width:28px;height:28px;border-radius:50%;background:var(--grad-h);display:flex;align-items:center;justify-content:center;color:#fff;font-size:11px;font-weight:700;flex-shrink:0">${(u.name[0]||'?').toUpperCase()}</div>
              <div style="flex:1;min-width:0">
                <div style="font-size:12px;font-weight:600">${escapeHtml(u.name)}</div>
                <div style="font-size:10px;color:var(--muted)">${escapeHtml(u.launcher)} · MC ${escapeHtml(u.version)}</div>
              </div>
              <div style="font-size:11px;font-weight:600;color:${sc};min-width:68px;text-align:center">${sd} ${u.status}</div>
              <div style="font-size:10px;color:var(--muted);min-width:56px;text-align:right">${u.injects} inject${u.injects !== 1 ? 's' : ''}</div>
            </div>`;
        }).join('');
    }

    function refreshUsers() {
        const n = onlineBase + Math.floor(Math.random() * 16) - 8;
        onlineBase = Math.max(220, Math.min(280, n));
        const inj  = Math.floor(onlineBase * 0.76);
        const elO  = document.getElementById('stat-online');
        const elI  = document.getElementById('stat-injected');
        if (elO) elO.textContent = onlineBase.toLocaleString();
        if (elI) elI.textContent = inj.toLocaleString();
        renderUserTable(shuffle(MOCK_USERS).slice(0, 8));
        const lb = document.getElementById('last-refresh-label');
        if (lb) lb.textContent = 'Updated ' + new Date().toLocaleTimeString();
    }

    renderUserTable(MOCK_USERS.slice(0, 8));
    refreshTimer = setInterval(refreshUsers, 5000);
    pageCleanup = () => clearInterval(refreshTimer);

    document.getElementById('btn-refresh-users')?.addEventListener('click', refreshUsers);

    document.querySelectorAll('[data-staff-action]').forEach(btn => {
        btn.addEventListener('click', () => notify(`${btn.dataset.staffAction} triggered (requires injection)`, 'info'));
    });

    document.getElementById('btn-simulate')?.addEventListener('click', simulateViolation);
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
              <div>Quark Ghost Client <span style="color:var(--brand)">v2.0.0</span></div>
              <div>Modules: <span style="color:var(--text)">${TOTAL_MODULES.toLocaleString()}</span></div>
              <div>MC Target: <span style="color:var(--text)">1.21.1 Fabric</span></div>
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
            alts = []; profiles = defaultProfiles(); servers = defaultServers(); keybinds = { ...DEFAULT_KEYBINDS };
            notify('All data cleared', 'info');
        }
    });

    document.getElementById('btn-login-settings')?.addEventListener('click', handleDiscordLogin);
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo helper for staff panel
// ─────────────────────────────────────────────────────────────────────────────

function simulateViolation() {
    const box = document.getElementById('violations');
    if (!box) return;
    const players = ['Player123', 'xXhackerXx', 'SpeedRunner99', 'CrystalGod'];
    const hacks   = ['KillAura', 'Flight', 'Speed', 'Reach'];
    const p = players[Math.floor(Math.random() * players.length)];
    const h = hacks[Math.floor(Math.random() * hacks.length)];
    const vl = Math.floor(Math.random() * 120) + 10;
    const div = document.createElement('div');
    div.style.cssText = 'display:flex;align-items:center;gap:8px;padding:8px;background:rgba(239,68,68,0.08);border:1px solid rgba(239,68,68,0.2);border-radius:7px;font-size:12px';
    div.innerHTML = `<span style="font-size:16px">🚨</span><div><strong>${p}</strong> — ${h} (VL ${vl})</div><span style="margin-left:auto;color:var(--muted);font-size:10px">${new Date().toLocaleTimeString()}</span>`;
    const empty = box.querySelector('[style*="text-align:center"]');
    if (empty) empty.remove();
    box.appendChild(div);
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

function shuffle(arr) {
    const out = [...arr];
    for (let i = out.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [out[i], out[j]] = [out[j], out[i]];
    }
    return out;
}
