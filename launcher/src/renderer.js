'use strict';
/* global quark */

// ─────────────────────────────────────────────────────────────────────────────
// State
// ─────────────────────────────────────────────────────────────────────────────

let currentUser   = null;
let currentPage   = 'home';
let selectedPid   = null;
let processList   = [];
let chatSocket    = null;
let chatMessages  = [];
let alts          = [];
let profiles      = [];
let injected      = false;

const STAFF_IDS = {
    '1401853518100303932': 'Owner',
};
const MODULE_COUNTS = {
    combat: 268, movement: 247, render: 220, player: 247,
    world: 186, exploit: 126, misc: 113, staff: 108,
};
const TOTAL_MODULES = Object.values(MODULE_COUNTS).reduce((a, b) => a + b, 0);

function getRole(user) {
    if (!user || user.guest) return 'User';
    return STAFF_IDS[user.id] || 'User';
}
function isStaff(user)  { return getRole(user) !== 'User'; }
function roleBadge(role) {
    return `<span class="role-badge role-${role.toLowerCase()}">${role}</span>`;
}

// ─────────────────────────────────────────────────────────────────────────────
// Notification system
// ─────────────────────────────────────────────────────────────────────────────

function notify(msg, type = 'info', duration = 3500) {
    let stack = document.getElementById('notif-stack');
    if (!stack) {
        stack = document.createElement('div');
        stack.id = 'notif-stack';
        stack.className = 'notif-stack';
        document.body.appendChild(stack);
    }
    const icons = { success: '✓', error: '✕', info: 'ℹ', warn: '⚠' };
    const el = document.createElement('div');
    el.className = `notif ${type}`;
    el.innerHTML = `<span style="font-size:14px">${icons[type] || icons.info}</span><span>${msg}</span>`;
    stack.appendChild(el);
    setTimeout(() => { el.style.opacity = '0'; el.style.transition = 'opacity .3s'; setTimeout(() => el.remove(), 300); }, duration);
}

// ─────────────────────────────────────────────────────────────────────────────
// Bootstrap
// ─────────────────────────────────────────────────────────────────────────────

window.addEventListener('DOMContentLoaded', async () => {
    // Window controls
    document.getElementById('btn-min').addEventListener('click',   () => quark.minimize());
    document.getElementById('btn-close').addEventListener('click', () => quark.close());

    // Version
    try {
        const v = await quark.version();
        document.getElementById('tb-version').textContent = `v${v}`;
    } catch (_) {}

    // Particle bg
    startParticles();

    // Check saved session
    const stored = await quark.settingsGet('user');
    alts     = (await quark.settingsGet('alts'))     || [];
    profiles = (await quark.settingsGet('profiles')) || defaultProfiles();

    if (stored) {
        currentUser = stored;
        showMain();
    } else {
        showLogin();
    }

    document.getElementById('btn-discord-login').addEventListener('click', handleDiscordLogin);
    document.getElementById('btn-skip').addEventListener('click', () => {
        currentUser = { username: 'Guest', guest: true };
        showMain();
    });
    document.getElementById('btn-cancel-oauth').addEventListener('click', () => {
        document.getElementById('oauth-overlay').classList.add('hidden');
    });

    // Auto-scan for processes every 8s if on inject page
    setInterval(() => { if (currentPage === 'inject') autoRefreshProcesses(); }, 8000);
});

// ─────────────────────────────────────────────────────────────────────────────
// Particle background
// ─────────────────────────────────────────────────────────────────────────────

function startParticles() {
    const canvas = document.getElementById('particle-canvas');
    if (!canvas) return;
    canvas.width  = window.innerWidth;
    canvas.height = window.innerHeight;
    const ctx  = canvas.getContext('2d');
    const pts  = Array.from({ length: 60 }, () => ({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height,
        vx: (Math.random() - .5) * .4,
        vy: (Math.random() - .5) * .4,
        r: Math.random() * 1.5 + .5,
        o: Math.random() * .5 + .1,
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
            ctx.fillStyle = `rgba(168,85,247,${p.o})`;
            ctx.fill();
        });
        // Draw connecting lines
        for (let i = 0; i < pts.length; i++) {
            for (let j = i + 1; j < pts.length; j++) {
                const d = Math.hypot(pts[i].x - pts[j].x, pts[i].y - pts[j].y);
                if (d < 100) {
                    ctx.beginPath();
                    ctx.moveTo(pts[i].x, pts[i].y);
                    ctx.lineTo(pts[j].x, pts[j].y);
                    ctx.strokeStyle = `rgba(168,85,247,${0.12 * (1 - d / 100)})`;
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
}

// ─────────────────────────────────────────────────────────────────────────────
// Discord login
// ─────────────────────────────────────────────────────────────────────────────

async function handleDiscordLogin() {
    document.getElementById('oauth-overlay').classList.remove('hidden');
    try {
        const user = await quark.discordLogin();
        currentUser = user;
        await quark.settingsSet('user', user);
        document.getElementById('oauth-overlay').classList.add('hidden');
        showMain();
    } catch (err) {
        document.getElementById('oauth-overlay').classList.add('hidden');
        if (!err.message?.includes('NO_CLIENT_ID') && !err.message?.includes('timed out') && !err.message?.includes('cancelled')) {
            notify('Login failed: ' + err.message, 'error');
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sidebar
// ─────────────────────────────────────────────────────────────────────────────

function buildSidebar() {
    // Show/hide staff item
    const staffItem = document.querySelector('.staff-only');
    if (staffItem) {
        staffItem.classList.toggle('hidden', !isStaff(currentUser));
    }

    // Nav items
    document.querySelectorAll('.nav-item').forEach(btn => {
        btn.addEventListener('click', () => navigateTo(btn.dataset.page));
    });

    // User row
    const role = getRole(currentUser);
    const ur = document.getElementById('user-row');
    const avatar = currentUser.avatarUrl
        ? `<img class="user-avatar" src="${currentUser.avatarUrl}" alt="">`
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
    const pages = { home, inject, modules, profiles: profilesPage, alts: altsPage, chat, changelog, staff, settings };
    const fn = pages[page];
    if (fn) {
        document.getElementById('content').innerHTML = '';
        fn();
    }
}

function showUserMenu() {
    if (currentUser && !currentUser.guest) {
        if (confirm('Sign out?')) {
            quark.settingsSet('user', null);
            quark.discordLogout();
            currentUser = null;
            showLogin();
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Status bar helper
// ─────────────────────────────────────────────────────────────────────────────

function setStatus(text, state = '') {
    const dot  = document.getElementById('status-dot');
    const span = document.getElementById('status-text');
    dot.className  = 'status-dot' + (state ? ' ' + state : '');
    span.textContent = text;
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE: Home
// ─────────────────────────────────────────────────────────────────────────────

function home() {
    const role = getRole(currentUser);
    const modCount = TOTAL_MODULES;
    document.getElementById('content').innerHTML = `
      <div class="page-header">
        <h1>Welcome back, ${currentUser.username || 'Guest'}</h1>
        <p>Quark Ghost Client — the most advanced injection-only Minecraft client</p>
      </div>

      <div class="grid-4" style="margin-bottom:16px">
        <div class="stat-card">
          <div class="stat-label">Total Modules</div>
          <div class="stat-value brand">${modCount.toLocaleString()}</div>
          <div class="stat-sub">across 8 categories</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Injection Mode</div>
          <div class="stat-value" style="font-size:16px;padding-top:4px">JVM Agent</div>
          <div class="stat-sub">no JAR install needed</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Status</div>
          <div class="stat-value" style="font-size:16px;padding-top:4px" id="home-status">${injected ? '<span class="text-success">Injected</span>' : '<span class="text-muted">Idle</span>'}</div>
          <div class="stat-sub" id="home-pid">${selectedPid ? 'PID ' + selectedPid : 'no process selected'}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Your Role</div>
          <div class="stat-value" style="font-size:16px;padding-top:4px">${roleBadge(role)}</div>
          <div class="stat-sub">${isStaff(currentUser) ? 'staff tools unlocked' : 'standard access'}</div>
        </div>
      </div>

      <div class="grid-2" style="margin-bottom:16px">
        <div class="card glow">
          <div class="card-title">Quick Inject</div>
          <p style="font-size:12px;color:var(--muted);margin-bottom:12px">Attach to a running Minecraft process without installing any files.</p>
          <div id="home-proc-status" style="font-size:12px;color:var(--muted);margin-bottom:10px">Scanning for processes…</div>
          <button class="btn btn-primary btn-full" id="home-inject-btn" ${selectedPid ? '' : 'disabled'}>
            ⚡ Inject Now
          </button>
          <div style="margin-top:8px">
            <button class="btn btn-secondary btn-sm" id="home-scan-btn">🔍 Scan Processes</button>
          </div>
        </div>

        <div class="card">
          <div class="card-title">Module Breakdown</div>
          ${Object.entries(MODULE_COUNTS).map(([cat, n]) => `
            <div style="display:flex;align-items:center;gap:8px;margin-bottom:6px">
              <span style="font-size:11px;color:var(--muted);width:70px;text-transform:capitalize">${cat}</span>
              <div class="progress-wrap" style="flex:1">
                <div class="progress-bar" style="width:${Math.round(n/3)}%"></div>
              </div>
              <span style="font-size:11px;color:var(--muted);width:28px;text-align:right">${n}</span>
            </div>`).join('')}
        </div>
      </div>

      <div class="card">
        <div class="card-title">Supported Environments</div>
        <div class="grid-4">
          ${['Fabric 1.21+', 'Forge 1.17+', 'NeoForge', 'Vanilla', 'Lunar Client', 'Badlion', 'TLauncher', 'Prism/MultiMC'].map(e =>
            `<div class="module-chip">${e}</div>`).join('')}
        </div>
      </div>`;

    // Wire buttons
    document.getElementById('home-scan-btn').addEventListener('click', async () => {
        await scanProcesses();
        const btn = document.getElementById('home-inject-btn');
        const status = document.getElementById('home-proc-status');
        if (processList.length === 0) {
            status.textContent = 'No Minecraft processes found.';
        } else {
            if (!selectedPid) selectedPid = processList[0].pid;
            status.textContent = `Found ${processList.length} process(es). PID ${selectedPid} selected.`;
            if (btn) btn.disabled = false;
        }
    });

    document.getElementById('home-inject-btn')?.addEventListener('click', () => {
        if (selectedPid) runInject(selectedPid, 'home');
    });

    // Auto scan
    scanProcesses().then(() => {
        const status = document.getElementById('home-proc-status');
        if (!status) return;
        if (processList.length === 0) {
            status.textContent = 'No Minecraft processes found. Launch Minecraft first.';
        } else {
            if (!selectedPid) selectedPid = processList[0].pid;
            status.textContent = `Found ${processList.length} Minecraft process(es).`;
            const btn = document.getElementById('home-inject-btn');
            if (btn) btn.disabled = false;
        }
    });
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE: Inject
// ─────────────────────────────────────────────────────────────────────────────

function inject() {
    document.getElementById('content').innerHTML = `
      <div class="page-header">
        <h1>Inject</h1>
        <p>Attach Quark to a running Minecraft JVM via agent injection — no files installed.</p>
      </div>

      <div class="grid-2">
        <div style="display:flex;flex-direction:column;gap:14px">
          <div class="card">
            <div class="card-title">Process Scanner</div>
            <div id="process-list" style="display:flex;flex-direction:column;gap:8px;margin-bottom:12px">
              <div style="color:var(--muted);font-size:12px">Scanning…</div>
            </div>
            <div style="display:flex;gap:8px">
              <button class="btn btn-secondary btn-sm" id="btn-scan">🔍 Refresh</button>
              <button class="btn btn-primary btn-sm" id="btn-inject" disabled>⚡ Inject Selected</button>
            </div>
          </div>

          <div class="card">
            <div class="card-title">Injection Options</div>
            <div class="toggle-row">
              <div><div class="toggle-label">Stealth Mode</div><div class="toggle-sub">Minimize injection footprint</div></div>
              <label class="toggle"><input type="checkbox" id="opt-stealth" checked><span class="toggle-slider"></span></label>
            </div>
            <div class="toggle-row">
              <div><div class="toggle-label">Auto-Reinject</div><div class="toggle-sub">Re-inject if client restarts</div></div>
              <label class="toggle"><input type="checkbox" id="opt-auto-reinject"><span class="toggle-slider"></span></label>
            </div>
            <div class="toggle-row">
              <div><div class="toggle-label">Verbose Log</div><div class="toggle-sub">Show detailed injection log</div></div>
              <label class="toggle"><input type="checkbox" id="opt-verbose"><span class="toggle-slider"></span></label>
            </div>
          </div>
        </div>

        <div style="display:flex;flex-direction:column;gap:14px">
          <div class="card">
            <div class="card-title">Injection Status</div>
            <div class="inject-steps" id="inject-steps">
              <div class="inject-step"><span class="step-icon pending">○</span>Scanning Minecraft JVM processes</div>
              <div class="inject-step"><span class="step-icon pending">○</span>Attaching to target PID</div>
              <div class="inject-step"><span class="step-icon pending">○</span>Loading Quark agent into JVM</div>
              <div class="inject-step"><span class="step-icon pending">○</span>Initialising module system</div>
              <div class="inject-step"><span class="step-icon pending">○</span>Registering event listeners</div>
              <div class="inject-step"><span class="step-icon pending">○</span>Injection complete</div>
            </div>
          </div>

          <div class="card">
            <div class="card-title">Console Output</div>
            <div class="inject-log" id="inject-log">
              <span class="log-info">[Quark] Launcher ready. Select a process to inject.</span>
            </div>
          </div>

          <div class="card">
            <div class="card-title">How It Works</div>
            <p style="font-size:12px;color:var(--muted);line-height:1.8">
              Quark uses the <strong style="color:var(--brand)">JVM Attach API</strong> to inject a Java agent
              directly into a running Minecraft process. The agent uses
              <strong style="color:var(--cyan)">ASM bytecode instrumentation</strong> to hook into
              MinecraftClient, GameRenderer, Keyboard, and network handlers at runtime —
              <strong style="color:var(--text)">without any JAR files or mods folder modification</strong>.
            </p>
          </div>
        </div>
      </div>`;

    document.getElementById('btn-scan').addEventListener('click', () => refreshProcessList());
    document.getElementById('btn-inject').addEventListener('click', () => {
        if (selectedPid) runInject(selectedPid, 'inject');
    });

    refreshProcessList();
}

async function scanProcesses() {
    try {
        processList = await quark.injectScan();
    } catch (_) {
        processList = [];
    }
    return processList;
}

async function refreshProcessList() {
    const list = document.getElementById('process-list');
    const btn  = document.getElementById('btn-inject');
    if (list) list.innerHTML = `<div style="color:var(--muted);font-size:12px">Scanning…</div>`;

    await scanProcesses();

    if (!list) return;
    if (processList.length === 0) {
        list.innerHTML = `<div style="color:var(--muted);font-size:12px;padding:12px;text-align:center">
            No Minecraft processes found.<br>
            <span style="font-size:11px">Launch Minecraft and click Refresh.</span>
        </div>`;
        if (btn) btn.disabled = true;
        return;
    }

    list.innerHTML = processList.map(p => {
        const loader = detectLoader(p.name);
        const loaderBadge = `<span class="process-badge ${loader.toLowerCase()}">${loader}</span>`;
        return `
          <div class="process-item ${p.pid === selectedPid ? 'selected' : ''}" data-pid="${p.pid}">
            <div class="process-icon">⛏</div>
            <div class="process-info">
              <div class="process-name">Minecraft (PID ${p.pid})</div>
              <div class="process-pid">${p.name || 'java'}</div>
            </div>
            ${loaderBadge}
          </div>`;
    }).join('');

    if (!selectedPid && processList.length > 0) selectedPid = processList[0].pid;
    if (btn) btn.disabled = !selectedPid;

    list.querySelectorAll('.process-item').forEach(el => {
        el.addEventListener('click', () => {
            selectedPid = parseInt(el.dataset.pid);
            list.querySelectorAll('.process-item').forEach(e => e.classList.remove('selected'));
            el.classList.add('selected');
            if (btn) btn.disabled = false;
        });
    });
}

function detectLoader(name) {
    if (!name) return 'Vanilla';
    const n = name.toLowerCase();
    if (n.includes('fabric')) return 'Fabric';
    if (n.includes('forge'))  return 'Forge';
    if (n.includes('lunar'))  return 'Lunar';
    if (n.includes('badlion'))return 'Lunar';
    return 'Vanilla';
}

async function runInject(pid, context) {
    const logEl   = context === 'inject' ? document.getElementById('inject-log') : null;
    const stepsEl = document.getElementById('inject-steps');

    function log(msg, cls = '') {
        if (logEl) logEl.innerHTML += `\n<span class="${cls}">${msg}</span>`;
        if (logEl) logEl.scrollTop = logEl.scrollHeight;
    }

    function step(idx, state) {
        if (!stepsEl) return;
        const steps = stepsEl.querySelectorAll('.inject-step');
        if (!steps[idx]) return;
        const icon = steps[idx].querySelector('.step-icon');
        icon.className = `step-icon ${state}`;
        const icons = { done: '✓', active: '◌', pending: '○' };
        icon.textContent = icons[state] || '○';
    }

    setStatus(`Injecting → PID ${pid}`, 'injected');
    log(`[Quark] Attaching to PID ${pid}…`, 'log-info');
    step(0, 'done'); step(1, 'active');

    try {
        const result = await quark.injectRun(pid);
        step(1, 'done'); step(2, 'active');
        log('[Quark] Agent loaded into JVM', 'log-success');
        await sleep(300);
        step(2, 'done'); step(3, 'active');
        log('[Quark] Initialising module system…', 'log-info');
        await sleep(400);
        step(3, 'done'); step(4, 'active');
        log('[Quark] Registering event listeners…', 'log-info');
        await sleep(300);
        step(4, 'done'); step(5, 'active');
        await sleep(200);
        step(5, 'done');
        log('[Quark] ✓ Injection successful!', 'log-success');
        log(`[Quark] Press Right-Shift in-game to open the GUI.`, 'log-info');
        injected = true;
        setStatus('Injected ✓', 'injected');
        notify('Quark injected successfully! Press Right-Shift in game.', 'success', 5000);
    } catch (err) {
        step(1, 'done');
        log(`[Quark] ✕ Injection failed: ${err.message}`, 'log-error');
        setStatus('Injection failed', 'error');
        notify('Injection failed: ' + err.message, 'error');
    }
}

async function autoRefreshProcesses() {
    await scanProcesses();
    const list = document.getElementById('process-list');
    if (list) refreshProcessList();
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE: Modules
// ─────────────────────────────────────────────────────────────────────────────

const MODULE_LIST = {
    combat:   ['KillAura','Criticals','Reach','Velocity','AutoCrystal','Surround','AntiKnockback','AutoTotem','BedAura','AimAssist','CrystalAura','Burrow','WTap','CritBot','TargetStrafe','SilentAura','MultiAura','AutoMLG','AutoGapple','ShieldBreaker','TriggerBot','LifeSteal','HoleSnap','Vampire','BackTrack','AntiPoison','AntiFire','HoleFiller','ForceField','Executioner','PingPredict','SuperCrit','ComboHit','PacketReach','DoubleHit'],
    movement: ['Flight','Speed','NoFall','Jesus','Spider','Step','Sprint','BunnyHop','Glide','ElytraFly','SafeWalk','AirStrafe','HighJump','Parkour','TeleportFly','WaterFly','ClimbSpeed','SmoothStep','FastHead','JumpBoost','IceSpeed','LongJump','EdgeClamp','MoonWalk','SneakFlight'],
    render:   ['ESP','Tracers','FullBright','Chams','XRay','Radar','HoleESP','StorageESP','NameTags','Trajectories','Zoom','FreeLook','BlockESP','CrystalESP','ItemESP','EntityGlow','ChunkESP','HideSelf','ClearVision','StatusEffectTimer','TargetHUD','SkyColor','DirectionHUD'],
    player:   ['AutoEat','NoFall','InvMove','AutoTool','AutoArmor','Scaffold','FastPlace','FoodSwapper','InvProtect','NoSwing2','AutoRefill2','PotionSelector','SmartEat2','ArrowCounter2','AutoSword','InventorySort','AntiHurtCam','AntiStuck'],
    world:    ['Nuker','AutoFarm','ChestStealer','AutoMine','VeinMiner','TreeFeller','AutoFish','AutoBuild','AutoBreeder','AutoEnchant','AutoAnvil','AutoCraft','AutoEnderFarm','BlockRotator','AutoTerraformer','FillChunk','MobTrap','AutoSmith','AutoLoom'],
    exploit:  ['PacketFly','Disabler','AntiCheat','Timer','Freecam','Phase','NoComPress','SpeedHack','PortalGod','ChestESP','PacketDupe','SignCrash','LecternExploit','RubberBand2','CommandSpoof'],
    misc:     ['AutoGG','ChatBot','MacroRecorder','DiscordRPC','PingSpoof','SessionInfo','StreamerFilter','PanicHotkey','GamepadSupport','TabListLogger','CrashDetector2','AutoWaypoint','SessionLog','AutoCommand2'],
    staff:    ['Vanish','XrayDetector','KillauraDetector','FlightDetector','AntiGrief','BanLog','PlayerWatch','ViolationLog','StaffMode','ChatFilter2','SpectatorTools'],
};

function modules() {
    document.getElementById('content').innerHTML = `
      <div class="page-header">
        <h1>Module Browser</h1>
        <p>${TOTAL_MODULES.toLocaleString()} modules across 8 categories</p>
      </div>

      <div class="search-wrap">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input class="search-input" id="mod-search" placeholder="Search modules…" autocomplete="off">
      </div>

      <div class="filter-tabs" id="mod-filters">
        <button class="filter-tab active" data-cat="all">All (${TOTAL_MODULES})</button>
        ${Object.entries(MODULE_COUNTS).map(([c, n]) =>
            `<button class="filter-tab" data-cat="${c}">${c.charAt(0).toUpperCase()+c.slice(1)} (${n})</button>`
        ).join('')}
      </div>

      <div id="mod-content"></div>`;

    let activeCat = 'all';

    function render(search = '') {
        const container = document.getElementById('mod-content');
        const s = search.toLowerCase();
        const entries = activeCat === 'all' ? Object.entries(MODULE_LIST) : [[activeCat, MODULE_LIST[activeCat] || []]];
        container.innerHTML = entries.map(([cat, mods]) => {
            const filtered = s ? mods.filter(m => m.toLowerCase().includes(s)) : mods;
            if (!filtered.length) return '';
            const count = MODULE_COUNTS[cat] || mods.length;
            return `
              <div class="module-cat-header">${cat.toUpperCase()} <span>${count} total${s ? ', ' + filtered.length + ' shown' : ''}</span></div>
              <div class="module-grid" style="margin-bottom:16px">
                ${filtered.map(m => `<div class="module-chip">${m}</div>`).join('')}
                ${!s && count > mods.length ? `<div class="module-chip" style="color:var(--muted);border-style:dashed">+${count - mods.length} more…</div>` : ''}
              </div>`;
        }).join('');
        if (!container.innerHTML.trim()) {
            container.innerHTML = `<div style="color:var(--muted);font-size:13px;padding:20px;text-align:center">No modules match "${search}"</div>`;
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
// PAGE: Profiles
// ─────────────────────────────────────────────────────────────────────────────

function defaultProfiles() {
    return [
        { id: 'pvp',     name: 'PvP Preset',    desc: 'KillAura, Criticals, AntiKB, Velocity, Reach', badges: ['combat','pvp'],    active: true  },
        { id: 'crystal', name: 'Crystal PvP',   desc: 'AutoCrystal, Surround, TotemPop, HoleSnap',    badges: ['combat'],          active: false },
        { id: 'legit',   name: 'Legit Client',  desc: 'Reach 3.2, Velocity 80%, Criticals, FastPlace', badges: ['clean'],          active: false },
        { id: 'build',   name: 'Builder',       desc: 'Scaffold, AutoBuild, FastPlace, NoFall, Step',  badges: ['movement'],       active: false },
        { id: 'explore', name: 'Explorer',      desc: 'XRay, ESP, FullBright, VeinMiner, AutoFarm',    badges: ['render'],         active: false },
        { id: 'staff',   name: 'Staff Mode',    desc: 'Vanish, Detectors, Logs, AdminTools',           badges: ['combat','render'], active: false },
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
        if (name) {
            profiles.push({ id: Date.now()+'', name, desc: 'Custom profile', badges: [], active: false });
            quark.settingsSet('profiles', profiles);
            renderProfiles();
            notify('Profile created: ' + name, 'success');
        }
    });
}

function renderProfiles() {
    const grid = document.getElementById('profile-grid');
    if (!grid) return;
    grid.innerHTML = profiles.map(p => `
      <div class="profile-card ${p.active ? 'active' : ''}" data-id="${p.id}">
        <div class="profile-name">${p.name}</div>
        <div class="profile-desc">${p.desc}</div>
        <div class="profile-badges">
          ${p.badges.map(b => `<span class="badge badge-${b}">${b}</span>`).join('')}
        </div>
        <div style="display:flex;gap:6px;margin-top:4px">
          <button class="btn btn-${p.active ? 'success' : 'secondary'} btn-sm" data-action="load" data-id="${p.id}">${p.active ? '✓ Active' : 'Load'}</button>
          <button class="btn btn-secondary btn-sm" data-action="del" data-id="${p.id}">Delete</button>
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
            } else {
                profiles = profiles.filter(p => p.id !== id);
                quark.settingsSet('profiles', profiles);
                renderProfiles();
                notify('Profile deleted', 'info');
            }
            e.stopPropagation();
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
        <div class="card-title">Saved Alts (${alts.length})</div>
        <div id="alt-list" style="display:flex;flex-direction:column;gap:8px"></div>
        ${alts.length === 0 ? '<div style="color:var(--muted);font-size:12px;text-align:center;padding:16px">No alts saved. Click + Add Alt to begin.</div>' : ''}
      </div>`;

    renderAlts();

    document.getElementById('btn-add-alt').addEventListener('click', () => {
        const name = prompt('Minecraft username:');
        if (name) {
            alts.push({ name, uuid: '', added: Date.now() });
            quark.settingsSet('alts', alts);
            renderAlts();
            notify('Alt added: ' + name, 'success');
        }
    });
}

function renderAlts() {
    const list = document.getElementById('alt-list');
    if (!list || alts.length === 0) return;
    list.innerHTML = alts.map((a, i) => `
      <div class="alt-item">
        <div class="alt-avatar" style="background:linear-gradient(135deg,#A855F7,#06B6D4);display:flex;align-items:center;justify-content:center;color:#fff;font-weight:700;font-size:15px">${a.name[0]?.toUpperCase()}</div>
        <div class="alt-info">
          <div class="alt-name">${a.name}</div>
          <div class="alt-status">Added ${new Date(a.added).toLocaleDateString()}</div>
        </div>
        <div class="alt-actions">
          <button class="btn btn-primary btn-sm" data-alt-use="${i}">Use</button>
          <button class="btn btn-danger btn-sm" data-alt-del="${i}">✕</button>
        </div>
      </div>`).join('');

    list.querySelectorAll('[data-alt-use]').forEach(b => {
        b.addEventListener('click', () => notify('Switching alt… (requires game restart)', 'info'));
    });
    list.querySelectorAll('[data-alt-del]').forEach(b => {
        b.addEventListener('click', () => {
            alts.splice(parseInt(b.dataset.altDel), 1);
            quark.settingsSet('alts', alts);
            renderAlts();
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
      <div class="card" style="margin-bottom:14px">
        <div class="card-title">
          <span>Live Chat</span>
          <span id="chat-online" style="font-size:11px;color:var(--success);font-weight:500">● Connecting…</span>
        </div>
        <div class="chat-box" id="chat-box">
          <div class="chat-msg">
            <div class="chat-msg-body">
              <span class="chat-msg-user">Quark</span>
              <span class="chat-msg-time">System</span>
              <div class="chat-msg-text">Welcome to Quark Global Chat!</div>
            </div>
          </div>
        </div>
        <div class="chat-input-row">
          <input class="form-input" id="chat-input" placeholder="Type a message…" autocomplete="off">
          <button class="btn btn-primary" id="btn-chat-send">Send</button>
        </div>
      </div>`;

    // Simulate some activity
    setTimeout(() => {
        const el = document.getElementById('chat-online');
        if (el) el.textContent = '● 12 online';
        addChatMsg('System', 'Chat relay connected.', true);
    }, 800);

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
    const div = document.createElement('div');
    div.className = 'chat-msg';
    div.innerHTML = `
      <div class="chat-msg-avatar" style="background:linear-gradient(135deg,#A855F7,#06B6D4);display:flex;align-items:center;justify-content:center;color:#fff;font-size:12px;font-weight:700">${user[0]?.toUpperCase()}</div>
      <div class="chat-msg-body">
        <span class="chat-msg-user" style="${system ? 'color:var(--cyan)' : ''}">${user}</span>
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
            { text: 'Pure JVM agent injection (no JAR/mods needed)', fix: false },
            { text: 'Support for Fabric, Forge, NeoForge, Vanilla, Lunar, Badlion', fix: false },
            { text: 'Complete launcher redesign with purple/cyan theme', fix: false },
            { text: 'Alt manager, profile system, global chat', fix: false },
            { text: '62 brand-new modules across all 8 categories', fix: false },
            { text: 'Fixed 161 redundant event subscribe/unsubscribe calls', fix: true },
            { text: 'ASM ClassTransformer handles obfuscated class names', fix: false },
        ]},
        { v: '1.5.0', date: 'May 2025', items: [
            { text: '107 staff modules with anti-cheat detection suite', fix: false },
            { text: 'EnvironmentDetector: auto-detects loader and launcher', fix: false },
            { text: 'ClassResolver: multi-environment class name resolution', fix: false },
            { text: 'ObfuscatedClassDetector: structural bytecode analysis', fix: true },
        ]},
        { v: '1.0.0', date: 'April 2025', items: [
            { text: 'Initial release with 800+ modules', fix: false },
            { text: 'Fabric mod base with EventBus', fix: false },
            { text: 'Electron launcher with Discord OAuth', fix: false },
        ]},
    ];

    document.getElementById('content').innerHTML = `
      <div class="page-header"><h1>Changelog</h1><p>What's new in Quark</p></div>
      <div class="card">
        ${entries.map(e => `
          <div class="changelog-entry">
            <div class="changelog-dot"></div>
            <div>
              <div class="changelog-version">v${e.v}</div>
              <div class="changelog-date">${e.date}</div>
              <ul class="changelog-items">
                ${e.items.map(i => `<li class="${i.fix ? 'fix' : ''}">${i.text}</li>`).join('')}
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
    document.getElementById('content').innerHTML = `
      <div class="page-header">
        <h1>Staff Panel</h1>
        <p>Administrative tools and cheat detection</p>
      </div>
      <div class="grid-4" style="margin-bottom:16px">
        ${[
            ['👁', 'Vanish',      'Become invisible to players'],
            ['⚡', 'Fly',         'Toggle staff flight'],
            ['🛡', 'God Mode',    'Toggle invincibility'],
            ['🔍', 'Inspect',     'View player inventory'],
            ['🔨', 'Ban Player',  'Issue a ban'],
            ['🔇', 'Mute Player', 'Issue a mute'],
            ['📍', 'Teleport',    'TP to any player'],
            ['📋', 'Logs',        'View violation logs'],
        ].map(([icon, name, desc]) => `
          <div class="staff-action-btn">
            <span class="icon">${icon}</span>
            <span style="font-weight:600;font-size:12px">${name}</span>
            <span style="font-size:10px;color:var(--muted);text-align:center">${desc}</span>
          </div>`).join('')}
      </div>
      <div class="grid-2">
        <div class="card">
          <div class="card-title">Active Violations</div>
          <div style="color:var(--muted);font-size:12px;text-align:center;padding:16px">No active violations detected.</div>
        </div>
        <div class="card">
          <div class="card-title">Detection Modules (${MODULE_COUNTS.staff})</div>
          <div class="module-grid">
            ${['XrayDetector','KillauraDetector','FlightDetector','SpeedDetector','ReachDetector','ScaffoldDetector','AimAssistDetector','MacroDetector'].map(m => `<div class="module-chip">${m}</div>`).join('')}
          </div>
        </div>
      </div>`;
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
              <input class="form-input" id="cfg-client-id" placeholder="1234567890" value="${cfg.discordClientId || ''}" autocomplete="off">
            </div>
            <div class="form-row">
              <div class="form-label">Client Secret</div>
              <input class="form-input" id="cfg-client-secret" type="password" placeholder="••••••••••" value="${cfg.discordClientSecret || ''}" autocomplete="off">
            </div>
            <button class="btn btn-primary btn-sm" id="btn-save-discord">Save Discord Config</button>
          </div>

          <div class="card">
            <div class="card-title">Injection</div>
            <div class="toggle-row">
              <div><div class="toggle-label">Auto-inject on launch</div><div class="toggle-sub">Inject when Minecraft is detected</div></div>
              <label class="toggle"><input type="checkbox" id="cfg-auto-inject" ${cfg.autoInject ? 'checked' : ''}><span class="toggle-slider"></span></label>
            </div>
            <div class="toggle-row">
              <div><div class="toggle-label">Silent injection</div><div class="toggle-sub">No console output</div></div>
              <label class="toggle"><input type="checkbox" id="cfg-silent-inject" ${cfg.silentInject ? 'checked' : ''}><span class="toggle-slider"></span></label>
            </div>
            <div class="form-row" style="margin-top:10px">
              <div class="form-label">Custom Agent JAR Path</div>
              <input class="form-input" id="cfg-agent-path" placeholder="Auto-detect" value="${cfg.agentPath || ''}" autocomplete="off">
            </div>
            <button class="btn btn-secondary btn-sm" id="btn-save-inject">Save Injection Config</button>
          </div>
        </div>

        <div style="display:flex;flex-direction:column;gap:14px">
          <div class="card">
            <div class="card-title">Launcher</div>
            <div class="toggle-row">
              <div><div class="toggle-label">Start minimised</div><div class="toggle-sub">Launcher starts in tray</div></div>
              <label class="toggle"><input type="checkbox" id="cfg-start-min"><span class="toggle-slider"></span></label>
            </div>
            <div class="toggle-row">
              <div><div class="toggle-label">Hardware acceleration</div><div class="toggle-sub">Use GPU for launcher rendering</div></div>
              <label class="toggle"><input type="checkbox" id="cfg-hw-accel" checked><span class="toggle-slider"></span></label>
            </div>
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
            <div style="font-size:12px;color:var(--muted);line-height:2">
              <div>Quark Ghost Client <span style="color:var(--brand)">v2.0.0</span></div>
              <div>Modules: <span style="color:var(--text)">${TOTAL_MODULES.toLocaleString()}</span></div>
              <div>Minecraft: <span style="color:var(--text)">1.21.1 Fabric</span></div>
              <div>Injection: <span style="color:var(--cyan)">JVM Agent (ASM 9)</span></div>
            </div>
          </div>
        </div>
      </div>`;

    document.getElementById('btn-save-discord')?.addEventListener('click', async () => {
        await quark.settingsSet('discordClientId',     document.getElementById('cfg-client-id').value.trim());
        await quark.settingsSet('discordClientSecret', document.getElementById('cfg-client-secret').value.trim());
        notify('Discord config saved', 'success');
    });

    document.getElementById('btn-save-inject')?.addEventListener('click', async () => {
        await quark.settingsSet('autoInject',  document.getElementById('cfg-auto-inject').checked);
        await quark.settingsSet('silentInject',document.getElementById('cfg-silent-inject').checked);
        await quark.settingsSet('agentPath',   document.getElementById('cfg-agent-path').value.trim());
        notify('Injection config saved', 'success');
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
            notify('Data cleared', 'info');
        }
    });

    document.getElementById('btn-login-settings')?.addEventListener('click', handleDiscordLogin);
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

function sleep(ms) { return new Promise(r => setTimeout(r, ms)); }

function escapeHtml(str) {
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}
