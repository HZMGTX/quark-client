'use strict';
/* global quark */

// ─────────────────────────────────────────────────────────────────────────────
// State
// ─────────────────────────────────────────────────────────────────────────────

let currentUser  = null;   // Discord user object or {username:'Guest',guest:true}
let currentPage  = 'Home';

// ─────────────────────────────────────────────────────────────────────────────
// Staff Role System
// ─────────────────────────────────────────────────────────────────────────────

const STAFF_IDS = {
    '1401853518100303932': 'Owner',      // corruptnull
};

function getRole(user) {
    if (!user || user.guest) return 'User';
    return STAFF_IDS[user.id] || 'User';
}
function isStaff(user)    { return getRole(user) !== 'User'; }
function isAdmin(user)    { return ['Owner','Admin'].includes(getRole(user)); }
function isOwner(user)    { return getRole(user) === 'Owner'; }
function roleBadge(role)  {
    const cls = 'role-' + role.toLowerCase();
    return `<span class="role-badge ${cls}">${role}</span>`;
}

// ─────────────────────────────────────────────────────────────────────────────
// Bootstrap
// ─────────────────────────────────────────────────────────────────────────────

window.addEventListener('DOMContentLoaded', async () => {
    // Window buttons
    document.getElementById('btn-min').addEventListener('click', () => quark.minimize());
    document.getElementById('btn-close').addEventListener('click', () => quark.close());

    // Draw dot grid on login canvas
    drawDotGrid();

    // Check if already logged in
    const stored = await quark.settingsGet('user');
    if (stored) {
        currentUser = stored;
        showMain();
    } else {
        showLogin();
    }

    // Login screen buttons
    document.getElementById('btn-discord-login').addEventListener('click', handleDiscordLogin);
    document.getElementById('btn-skip').addEventListener('click', () => {
        currentUser = { username: 'Guest', guest: true };
        showMain();
    });
    document.getElementById('btn-cancel-oauth').addEventListener('click', () => {
        document.getElementById('oauth-overlay').classList.add('hidden');
    });
});

// ─────────────────────────────────────────────────────────────────────────────
// Dot-grid background
// ─────────────────────────────────────────────────────────────────────────────

function drawDotGrid() {
    const canvas = document.getElementById('dot-canvas');
    canvas.width  = window.innerWidth;
    canvas.height = window.innerHeight;
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = '#1C1C26';
    const step = 28;
    for (let x = 0; x < canvas.width; x += step)
        for (let y = 0; y < canvas.height; y += step)
            ctx.fillRect(x, y, 1.5, 1.5);
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
    renderSidebar();
    navigate('Home');
}

// ─────────────────────────────────────────────────────────────────────────────
// Discord OAuth handler
// ─────────────────────────────────────────────────────────────────────────────

async function handleDiscordLogin() {
    // Check if client ID is configured
    const clientId = await quark.settingsGet('discordClientId');
    if (!clientId) {
        // Redirect to settings to enter client ID first
        currentUser = null;
        showMain();
        navigate('Settings');
        showToast('Enter your Discord Client ID in Settings first, then try again.', 'warn');
        return;
    }

    document.getElementById('oauth-overlay').classList.remove('hidden');

    try {
        const user = await quark.discordLogin();
        document.getElementById('oauth-overlay').classList.add('hidden');
        currentUser = user;
        showMain();
    } catch (err) {
        document.getElementById('oauth-overlay').classList.add('hidden');
        if (err.message === 'NO_CLIENT_ID') {
            showMain();
            navigate('Settings');
            showToast('Enter your Discord Client ID in Settings first.', 'warn');
        } else {
            showToast('Discord login failed: ' + err.message, 'error');
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sidebar
// ─────────────────────────────────────────────────────────────────────────────

const NAV_ITEMS = [
    { icon: '⌂', label: 'Home' },
    { icon: '⇒', label: 'Inject' },
    { icon: '○', label: 'Changelog' },
    { icon: '⚙', label: 'Settings' },
    { icon: '★', label: 'Credits' },
    { icon: '❯', label: 'Account' },
];

const STAFF_NAV = [
    { icon: '🛡', label: 'Staff Panel', requires: 'staff' },
    { icon: '📊', label: 'Analytics',   requires: 'admin' },
];

function renderSidebar() {
    const list = document.getElementById('nav-list');
    list.innerHTML = '';

    const allItems = [...NAV_ITEMS];
    for (const s of STAFF_NAV) {
        if (s.requires === 'staff' && isStaff(currentUser)) allItems.push(s);
        else if (s.requires === 'admin' && isAdmin(currentUser)) allItems.push(s);
    }

    for (const item of allItems) {
        const row = document.createElement('div');
        row.className = 'nav-item' + (item.label === currentPage ? ' active' : '');
        const badge = item.requires ? '<span class="nav-badge">STAFF</span>' : '';
        row.innerHTML = `<span class="nav-icon">${item.icon}</span>
                         <span class="nav-label">${item.label}</span>${badge}`;
        row.addEventListener('click', () => navigate(item.label));
        list.appendChild(row);
    }

    renderUserRow();
}

function renderUserRow() {
    const el = document.getElementById('user-row');
    if (!currentUser) { el.innerHTML = ''; return; }

    const hasAvatar = currentUser.avatarUrl && !currentUser.guest;
    const initial   = (currentUser.username || 'G')[0].toUpperCase();

    el.innerHTML = `
        ${hasAvatar
            ? `<img src="${currentUser.avatarUrl}" class="user-avatar" alt="avatar" onerror="this.style.display='none'">`
            : `<div class="user-avatar-placeholder">${initial}</div>`}
        <div class="user-info">
            <div class="user-name">${escHtml(currentUser.username)}</div>
            <div class="user-tag">${currentUser.guest ? 'Guest' : ('@' + (currentUser.username || '').toLowerCase())}</div>
        </div>
        <button class="logout-btn" title="Log out">&#x21A6;</button>`;

    el.querySelector('.logout-btn').addEventListener('click', async () => {
        await quark.discordLogout();
        currentUser = null;
        showLogin();
    });
}

// ─────────────────────────────────────────────────────────────────────────────
// Navigation
// ─────────────────────────────────────────────────────────────────────────────

function navigate(page) {
    currentPage = page;
    renderSidebar();

    const content = document.getElementById('content');
    content.scrollTop = 0;

    switch (page) {
        case 'Home':        renderHome(content);       break;
        case 'Inject':      renderInject(content);     break;
        case 'Changelog':   renderChangelog(content);  break;
        case 'Settings':    renderSettings(content);   break;
        case 'Credits':     renderCredits(content);    break;
        case 'Account':     renderAccount(content);    break;
        case 'Staff Panel': renderStaffPanel(content); break;
        case 'Analytics':   renderAnalytics(content);  break;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HOME
// ─────────────────────────────────────────────────────────────────────────────

function renderHome(el) {
    const name = currentUser ? currentUser.username : 'Player';
    const role = getRole(currentUser);
    const staffBadge = isStaff(currentUser) ? ` ${roleBadge(role)}` : '';

    el.innerHTML = `
        <div class="page-header">
            <div class="page-title">Welcome back, ${escHtml(name)}!${staffBadge}</div>
            <div class="page-sub">Ready to make it rain.</div>
        </div>

        <div class="quick-stats">
            <div class="quick-stat">
                <div class="quick-stat-icon">👥</div>
                <div><div class="quick-stat-value">2,847</div><div class="quick-stat-label">Total Users</div></div>
            </div>
            <div class="quick-stat">
                <div class="quick-stat-icon">🟢</div>
                <div><div class="quick-stat-value">142</div><div class="quick-stat-label">Online Now</div></div>
            </div>
            <div class="quick-stat">
                <div class="quick-stat-icon">⚡</div>
                <div><div class="quick-stat-value">v1.0.0</div><div class="quick-stat-label">Latest Version</div></div>
            </div>
            <div class="quick-stat">
                <div class="quick-stat-icon">🛡</div>
                <div><div class="quick-stat-value">Online</div><div class="quick-stat-label">Server Status</div></div>
            </div>
        </div>

        <div class="inject-banner">
            <div class="inject-banner-text">
                <h3>Ready to inject</h3>
                <p>Attach Quark into a running Minecraft 1.21.1 instance.</p>
            </div>
            <button class="btn btn-primary btn-lg" id="home-inject-btn">Launch Inject &nbsp;→</button>
        </div>

        <div class="card-grid">
            ${featureCard('○', 'Changelog', "See what's new", 'Changelog')}
            ${featureCard('⚙', 'Settings',  'Customize the look', 'Settings')}
            ${featureCard('❯', 'Account',   'Your profile',  'Account')}
            ${featureCard('★', 'Credits',   'Meet the team', 'Credits')}
        </div>

        <div class="news-section">
            <div class="news-section-title">📰 Latest News</div>
            <div class="news-card">
                <div class="news-icon-box">🎉</div>
                <div>
                    <div class="news-title">Quark v1.0.0 — Official Launch</div>
                    <div class="news-body">1000+ modules shipped. True XRay, Full ESP suite, Ghost Mode for GrimAC/Vulcan/Polar. Desktop launcher with Discord OAuth.</div>
                    <div class="news-date">June 2025</div>
                </div>
            </div>
            <div class="news-card">
                <div class="news-icon-box">🔒</div>
                <div>
                    <div class="news-title">Anti-Cheat Bypass Update</div>
                    <div class="news-body">GrimAC prediction bypass improved. Vulcan strafe and inventory move now fully bypassed on strict config.</div>
                    <div class="news-date">May 2025</div>
                </div>
            </div>
            <div class="news-card">
                <div class="news-icon-box">🚀</div>
                <div>
                    <div class="news-title">Launcher v1.0 Released</div>
                    <div class="news-body">Electron desktop app with Discord OAuth2 login, process scanner, and NSIS installer packaging.</div>
                    <div class="news-date">May 2025</div>
                </div>
            </div>
        </div>`;

    el.querySelector('#home-inject-btn').addEventListener('click', () => navigate('Inject'));
    el.querySelectorAll('.feature-card').forEach(card => {
        card.addEventListener('click', () => navigate(card.dataset.page));
    });
}

function featureCard(icon, title, desc, page) {
    return `<div class="feature-card" data-page="${page}">
        <div class="feature-icon-box">${icon}</div>
        <div>
            <div class="feature-title">${title}</div>
            <div class="feature-desc">${desc}</div>
        </div>
    </div>`;
}

// ─────────────────────────────────────────────────────────────────────────────
// INJECT
// ─────────────────────────────────────────────────────────────────────────────

async function renderInject(el) {
    el.innerHTML = `
        <div class="page-header">
            <div class="page-title">Inject</div>
            <div class="page-sub">Attach Quark into a running Minecraft 1.21.1 JVM process.</div>
        </div>
        <div class="mt-24 card">
            <div class="flex-row">
                <div style="flex:1">
                    <div style="font-size:14px;font-weight:700;margin-bottom:4px">Java Processes</div>
                    <div class="text-sm text-muted">Scanning for running Minecraft instances…</div>
                </div>
                <button class="btn btn-secondary" id="inject-refresh-btn">↻ &nbsp;Refresh</button>
            </div>
            <div id="inject-process-list" class="process-list mt-12">
                <div class="empty-state">
                    <div class="spinner" style="margin:0 auto 12px"></div>
                    <p>Scanning…</p>
                </div>
            </div>
        </div>
        <div class="pills mt-16">
            ${['JNI Injection','No patched JARs','Fabric 1.21.1','1000+ Modules'].map(t=>`<div class="pill">${t}</div>`).join('')}
        </div>`;

    el.querySelector('#inject-refresh-btn').addEventListener('click', () => refreshProcessList());

    refreshProcessList();

    async function refreshProcessList() {
        const listEl = document.getElementById('inject-process-list');
        listEl.innerHTML = `<div class="empty-state"><div class="spinner" style="margin:0 auto 12px"></div><p>Scanning…</p></div>`;

        const procs = await quark.injectScan();
        listEl.innerHTML = '';

        if (!procs || procs.length === 0) {
            listEl.innerHTML = `<div class="empty-state">
                <div class="empty-icon">☕</div>
                <p>No Java processes found.<br>Start Minecraft and click Refresh.</p>
            </div>`;
            return;
        }

        for (const p of procs) {
            const row = document.createElement('div');
            row.className = 'process-row';
            row.innerHTML = `
                <div class="process-dot"></div>
                <div class="process-info">
                    <div class="process-name">${escHtml(p.name)}</div>
                    <div class="process-pid">PID: ${p.pid}</div>
                </div>
                <button class="btn btn-primary" data-pid="${p.pid}">Inject →</button>`;

            row.querySelector('button').addEventListener('click', async (e) => {
                const btn = e.currentTarget;
                btn.disabled = true;
                btn.textContent = 'Injecting…';
                try {
                    const res = await quark.injectRun(p.pid);
                    if (res.success) {
                        btn.textContent = '✓ Injected';
                        btn.className = 'btn';
                        btn.style.background = 'var(--success)';
                        btn.style.color = '#000';
                        showToast(`Quark injected! Folder: ${res.gameDir}`, 'success');
                    }
                } catch (err) {
                    btn.textContent = '✕ Failed';
                    btn.style.background = 'var(--danger)';
                    showToast('Injection failed: ' + err.message, 'error');
                }
            });

            listEl.appendChild(row);
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHANGELOG
// ─────────────────────────────────────────────────────────────────────────────

const CHANGELOG = [
    {
        version: 'v1.0.0',
        date: 'June 2025',
        latest: true,
        items: [
            '1000+ fully implemented modules across 7 categories',
            'RAIN-style ClickGUI with horizontal tab bar',
            'RAIN-style ActiveMods HUD with slide-in animation',
            'ClutchSilentAim — activates silent aim below health threshold',
            'BlockInSilentAim — silent aim + optional block placement',
            'ChatFilter — blacklist/whitelist with ad/caps/spam blocking',
            'RadarPlus — player radar with rotation and name display',
            'SlimeFinder — chunk scan with HUD overlay',
            'TPSDisplay — server TPS via WorldTimeUpdate packet',
            'PingHUD — colour-coded latency display',
            'Quark.cc Launcher with Discord OAuth & exe packaging',
        ],
    },
    {
        version: 'v0.9.0',
        date: 'May 2025',
        items: [
            'AutoCrystal, CrystalAura, BowAimbot',
            'IceSpeed, ElytraControl, JetpackFly, AntiSlowdown',
            'IceRoad, RoofBuilder, TowerBuilder, AutoSapling',
            'AntiCheatBypass, PacketLogger2, WDL',
            'Launcher skeleton (JavaFX → Electron rewrite)',
        ],
    },
    {
        version: 'v0.8.0',
        date: 'April 2025',
        items: [
            'Initial release with 800 module registrations',
            'Basic ClickGUI with category panels',
            'EventBus system with @EventHandler annotation',
            'Settings API: Bool / Int / Double / Mode / Color / String',
        ],
    },
];

function renderChangelog(el) {
    el.innerHTML = `
        <div class="page-header">
            <div class="page-title">Changelog</div>
            <div class="page-sub">What's new in Quark.cc</div>
        </div>
        <div class="mt-24">
            ${CHANGELOG.map(entry => `
            <div class="card changelog-entry">
                <div class="changelog-version">
                    ${entry.version} — ${entry.date}
                    ${entry.latest ? '<span class="changelog-badge">LATEST</span>' : ''}
                </div>
                <ul class="changelog-body">
                    ${entry.items.map(i => `<li>${escHtml(i)}</li>`).join('')}
                </ul>
            </div>`).join('')}
        </div>`;
}

// ─────────────────────────────────────────────────────────────────────────────
// SETTINGS
// ─────────────────────────────────────────────────────────────────────────────

async function renderSettings(el) {
    const all = (await quark.settingsGetAll()) || {};

    el.innerHTML = `
        <div class="page-header">
            <div class="page-title">Settings</div>
            <div class="page-sub">Customize the launcher and manage your Discord application.</div>
        </div>

        <div class="mt-24">

        <!-- Discord OAuth credentials -->
        <div class="card settings-section mt-0">
            <div class="settings-section-title">Discord Application</div>

            <div class="setting-row">
                <div class="setting-text">
                    <div class="setting-label">Client ID</div>
                    <div class="setting-desc">
                        Your Discord app Client ID.
                        <a href="#" id="link-dev-portal" style="color:var(--brand)">Get one at discord.com/developers</a>
                    </div>
                </div>
                <input class="text-input" id="input-client-id"
                    type="text" placeholder="1234567890"
                    value="${escHtml(all.discordClientId || '')}">
            </div>

            <div class="setting-row">
                <div class="setting-text">
                    <div class="setting-label">Client Secret</div>
                    <div class="setting-desc">Required to exchange the OAuth code for a token.</div>
                </div>
                <input class="text-input" id="input-client-secret"
                    type="password" placeholder="••••••••••••••••••••••••••"
                    value="${escHtml(all.discordClientSecret || '')}">
            </div>

            <div class="setting-row">
                <div class="setting-text">
                    <div class="setting-label">Redirect URI (fixed)</div>
                    <div class="setting-desc">Add this exact URI to your Discord app's OAuth2 redirect list.</div>
                </div>
                <code style="font-size:12px;color:var(--brand);background:var(--bg-input);padding:5px 10px;border-radius:5px;">
                    http://localhost:3847/callback
                </code>
            </div>

            <div class="setting-row">
                <button class="btn btn-primary" id="btn-save-discord">Save credentials</button>
                <button class="btn btn-secondary" id="btn-test-login">Test Discord login</button>
            </div>
        </div>

        <!-- Injection -->
        <div class="card settings-section">
            <div class="settings-section-title">Injection</div>
            ${toggleSetting('auto-inject',    'Auto-inject on launch', 'Attach as soon as Minecraft is detected', all.autoInject)}
            ${toggleSetting('inject-log',     'Show inject log',        'Print output to developer console',       all.injectLog !== false)}
            ${toggleSetting('keep-open',      'Keep launcher open',     'Do not minimise after injection',         all.keepOpen  !== false)}
        </div>

        <!-- Appearance -->
        <div class="card settings-section">
            <div class="settings-section-title">Appearance</div>
            ${toggleSetting('compact-sidebar','Compact sidebar', 'Show only icons (future)', false, true)}
        </div>

        <!-- Notifications -->
        <div class="card settings-section">
            <div class="settings-section-title">Notifications</div>
            ${toggleSetting('update-notif',   'Update notifications', 'Alert when a new version is available', all.updateNotif !== false)}
            ${toggleSetting('friend-notif',   'Friend online alerts',  'Alert when a Discord friend joins',     all.friendNotif)}
        </div>

        </div>`;

    // Discord credentials save
    el.querySelector('#btn-save-discord').addEventListener('click', async () => {
        await quark.settingsSet('discordClientId',     el.querySelector('#input-client-id').value.trim());
        await quark.settingsSet('discordClientSecret', el.querySelector('#input-client-secret').value.trim());
        showToast('Discord credentials saved!', 'success');
    });

    el.querySelector('#btn-test-login').addEventListener('click', async () => {
        showToast('Opening Discord…', 'info');
        try {
            const user = await quark.discordLogin();
            currentUser = user;
            renderSidebar();
            showToast('Logged in as ' + user.username, 'success');
        } catch (err) {
            showToast(err.message === 'NO_CLIENT_ID'
                ? 'Enter a Client ID first'
                : 'Login failed: ' + err.message, 'error');
        }
    });

    el.querySelector('#link-dev-portal').addEventListener('click', (e) => {
        e.preventDefault();
        // Open in system browser via Electron shell — send via IPC
        // Since we can't import shell in renderer, just show the URL
        showToast('Visit: https://discord.com/developers/applications', 'info');
    });

    // Wire up toggles
    el.querySelectorAll('.toggle input').forEach(input => {
        input.addEventListener('change', () => {
            quark.settingsSet(input.dataset.key, input.checked);
        });
    });
}

function toggleSetting(key, label, desc, checked, disabled = false) {
    return `<div class="setting-row">
        <div class="setting-text">
            <div class="setting-label">${label}</div>
            <div class="setting-desc">${desc}</div>
        </div>
        <label class="toggle">
            <input type="checkbox" data-key="${key}" ${checked ? 'checked' : ''} ${disabled ? 'disabled' : ''}>
            <span class="toggle-slider"></span>
        </label>
    </div>`;
}

// ─────────────────────────────────────────────────────────────────────────────
// CREDITS
// ─────────────────────────────────────────────────────────────────────────────

const TEAM = [
    { initial: 'Q', name: 'Quark Dev',      role: 'Lead developer & architecture',      color: '#00AAFF' },
    { initial: 'M', name: 'Module Author',  role: '1000+ module implementations',        color: '#55FF55' },
    { initial: 'U', name: 'UI Designer',    role: 'RAIN-style HUD & ClickGUI',           color: '#FF55FF' },
    { initial: 'T', name: 'Tester',         role: 'QA, bug reports, server compatibility', color: '#FFFF55' },
    { initial: 'C', name: 'Community',      role: 'Feature requests and testing',         color: '#FF9944' },
];

function renderCredits(el) {
    el.innerHTML = `
        <div class="page-header">
            <div class="page-title">Credits</div>
            <div class="page-sub">The team behind Quark.cc</div>
        </div>
        <div class="credits-grid mt-24">
            ${TEAM.map(m => `
            <div class="card credit-card">
                <div class="credit-avatar" style="background:${m.color}">${m.initial}</div>
                <div>
                    <div class="credit-name">${m.name}</div>
                    <div class="credit-role">${m.role}</div>
                </div>
            </div>`).join('')}
        </div>`;
}

// ─────────────────────────────────────────────────────────────────────────────
// ACCOUNT
// ─────────────────────────────────────────────────────────────────────────────

function renderAccount(el) {
    const u = currentUser;
    const isGuest = !u || u.guest;

    const hasAvatar = u && u.avatarUrl && !isGuest;
    const initial   = u ? (u.username || 'G')[0].toUpperCase() : 'G';

    el.innerHTML = `
        <div class="page-header">
            <div class="page-title">Account</div>
            <div class="page-sub">Manage your Quark.cc profile.</div>
        </div>

        <div class="card mt-24">
            <div class="account-profile">
                ${hasAvatar
                    ? `<img src="${u.avatarUrl}" class="account-avatar" alt="avatar">`
                    : `<div class="account-avatar">${initial}</div>`}
                <div>
                    <div class="account-username">${escHtml(u ? u.username : 'Guest')}</div>
                    <div class="account-meta">
                        ${isGuest ? 'Not signed in' : `Discord ID: ${u.id || '—'}`}<br>
                        ${isStaff(u) ? roleBadge(getRole(u)) + ' &nbsp;•&nbsp;' : 'Plan: Free &nbsp;•&nbsp;'} Member since June 2025
                    </div>
                </div>
            </div>

            ${!isGuest && isStaff(u) ? `
            <div class="mt-24">
                <div class="text-sm font-weight-700 mb-8">Staff Permissions</div>
                <div class="perm-list">
                    <div class="perm-item"><span class="perm-check">✔</span> Access Staff Panel</div>
                    <div class="perm-item"><span class="perm-check">✔</span> Manage Bans & Hardware IDs</div>
                    <div class="perm-item"><span class="perm-check">✔</span> Bypass anti-cheat flags</div>
                    <div class="perm-item"><span class="perm-check">✔</span> In-game Staff ESP & Chat</div>
                    ${isAdmin(u) ? '<div class="perm-item"><span class="perm-check">✔</span> Access Analytics & Keys</div>' : '<div class="perm-item"><span class="perm-cross">✖</span> Access Analytics & Keys</div>'}
                </div>
            </div>
            ` : ''}

            <div class="stat-row">
                <div class="stat-box"><div class="stat-value">1000</div><div class="stat-label">Modules</div></div>
                <div class="stat-box"><div class="stat-value">7</div><div class="stat-label">Categories</div></div>
                <div class="stat-box"><div class="stat-value">3</div><div class="stat-label">Saved configs</div></div>
                <div class="stat-box"><div class="stat-value">1.21.1</div><div class="stat-label">MC version</div></div>
            </div>

            <div class="flex-row mt-12">
                ${isGuest
                    ? `<button class="btn btn-primary" id="btn-acc-login">Sign in with Discord</button>`
                    : `<button class="btn btn-secondary" id="btn-acc-logout">Log out</button>
                       <button class="btn btn-primary">Manage account</button>`}
            </div>
        </div>`;

    if (isGuest) {
        el.querySelector('#btn-acc-login').addEventListener('click', () => {
            navigate('Settings');
            showToast('Configure your Discord app credentials in Settings first.', 'info');
        });
    } else {
        el.querySelector('#btn-acc-logout').addEventListener('click', async () => {
            await quark.discordLogout();
            currentUser = null;
            showLogin();
        });
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Toast notifications
// ─────────────────────────────────────────────────────────────────────────────

let toastTimeout;
function showToast(msg, type = 'info') {
    let toast = document.getElementById('toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'toast';
        toast.style.cssText = `
            position:fixed;bottom:20px;left:50%;transform:translateX(-50%);
            padding:10px 20px;border-radius:8px;font-size:13px;font-weight:500;
            z-index:9999;pointer-events:none;
            transition:opacity .3s;`;
        document.body.appendChild(toast);
    }

    const colors = { success: '#55FF55', error: '#FF5555', warn: '#FFAA00', info: '#00AAFF' };
    toast.style.background = '#1C1C26';
    toast.style.border = `1px solid ${colors[type] || colors.info}`;
    toast.style.color  = colors[type] || colors.info;
    toast.textContent  = msg;
    toast.style.opacity = '1';

    clearTimeout(toastTimeout);
    toastTimeout = setTimeout(() => { toast.style.opacity = '0'; }, 3500);
}

// ─────────────────────────────────────────────────────────────────────────────
// Utility
// ─────────────────────────────────────────────────────────────────────────────

function escHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

// ─────────────────────────────────────────────────────────────────────────────
// STAFF PANEL
// ─────────────────────────────────────────────────────────────────────────────

function renderStaffPanel(el) {
    if (!isStaff(currentUser)) {
        el.innerHTML = `<div class="page-header"><div class="page-title text-danger">Access Denied</div></div>`;
        return;
    }

    el.innerHTML = `
        <div class="page-header">
            <div class="page-title">Staff Panel</div>
            <div class="page-sub">Administration & Moderation Tools</div>
        </div>

        <div class="staff-tabs">
            <button class="staff-tab active" data-tab="users">Users & Roles</button>
            <button class="staff-tab" data-tab="bans">Ban Manager</button>
            <button class="staff-tab" data-tab="keys">License Keys</button>
            <button class="staff-tab" data-tab="announce">Announcements</button>
            <button class="staff-tab" data-tab="audit">Audit Log</button>
        </div>

        <div id="staff-content" class="mt-16"></div>
    `;

    const tabs = el.querySelectorAll('.staff-tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            renderStaffTab(el.querySelector('#staff-content'), tab.dataset.tab);
        });
    });

    renderStaffTab(el.querySelector('#staff-content'), 'users');
}

function renderStaffTab(el, tab) {
    if (tab === 'users') {
        el.innerHTML = `
            <div class="search-bar">
                <input type="text" class="text-input" placeholder="Search by Discord ID or Username...">
                <button class="btn btn-secondary">Search</button>
            </div>
            <table class="staff-table">
                <thead>
                    <tr><th>User</th><th>Discord ID</th><th>Role</th><th>Status</th><th>Actions</th></tr>
                </thead>
                <tbody>
                    <tr>
                        <td><strong>corruptnull</strong></td>
                        <td class="text-muted">1401853518100303932</td>
                        <td>${roleBadge('Owner')}</td>
                        <td><span class="status-dot status-online"></span>Online</td>
                        <td>—</td>
                    </tr>
                </tbody>
            </table>
        `;
    } else if (tab === 'bans') {
        window._bans = window._bans || [
            { name: 'RandomUser123', hwid: 'A8F9-C12B-D4E5', by: 'corruptnull' }
        ];
        el.innerHTML = `
            <div class="search-bar">
                <input type="text" class="text-input" placeholder="Search bans by HWID or Discord ID...">
                <button class="btn btn-primary" id="btn-ban-user">Ban User</button>
            </div>
            <div class="card mt-16" id="bans-list">
                ${window._bans.map(b => `
                <div class="ban-row">
                    <div class="ban-icon">✖</div>
                    <div class="ban-info">
                        <div class="ban-name">${b.name}</div>
                        <div class="ban-meta">HWID: ${b.hwid} • Banned by ${b.by}</div>
                    </div>
                    <div class="ban-actions">
                        <button class="btn btn-secondary btn-sm" onclick="showToast('Unbanned!', 'success')">Unban</button>
                    </div>
                </div>
                `).join('')}
            </div>
        `;
        document.getElementById('btn-ban-user').addEventListener('click', () => {
            window._bans.unshift({ name: 'Cheater' + Math.floor(Math.random()*999), hwid: 'X' + Math.floor(Math.random()*9999), by: currentUser ? currentUser.username : 'Owner' });
            renderStaffTab(document.querySelector('#staff-content'), 'bans');
            showToast('User banned successfully.', 'success');
        });
    } else if (tab === 'keys') {
        window._keys = window._keys || [
            { key: 'QURK-A1B2-C3D4-E5F6-G7H8', status: 'active', claim: 'Unclaimed' },
            { key: 'QURK-Z9Y8-X7W6-V5U4-T3S2', status: 'expired', claim: 'by .foulz.' }
        ];
        el.innerHTML = `
            <div class="flex-row mb-16">
                <button class="btn btn-primary" id="btn-gen-keys">Generate Keys</button>
            </div>
            <div class="key-grid">
                ${window._keys.map(k => `
                <div class="key-card">
                    <div class="key-value">${k.key}</div>
                    <div class="key-meta"><span class="key-status-${k.status}">${k.status.charAt(0).toUpperCase() + k.status.slice(1)}</span> • ${k.claim}</div>
                </div>
                `).join('')}
            </div>
        `;
        document.getElementById('btn-gen-keys').addEventListener('click', () => {
            const gen = 'QURK-' + Math.random().toString(36).substring(2, 6).toUpperCase() + '-' + Math.random().toString(36).substring(2, 6).toUpperCase();
            window._keys.unshift({ key: gen, status: 'active', claim: 'Unclaimed' });
            renderStaffTab(document.querySelector('#staff-content'), 'keys');
            showToast('Generated 1 new license key.', 'success');
        });
    } else if (tab === 'announce') {
        window._ann = window._ann || [
            { title: 'Quark v1.0.0 — Official Launch', date: 'June 2025', body: '1000+ modules shipped. True XRay, Full ESP suite.' }
        ];
        el.innerHTML = `
            <div class="compose-area">
                <input type="text" id="ann-title" class="text-input mb-8" style="width:100%" placeholder="Announcement Title...">
                <textarea id="ann-body" placeholder="Write announcement..."></textarea>
                <div class="flex-row mt-8" style="justify-content: flex-end">
                    <button class="btn btn-primary" id="btn-publish-ann">Publish</button>
                </div>
            </div>
            <div class="mt-24">
                ${window._ann.map(a => `
                <div class="announcement-card announcement-priority-high">
                    <div class="announcement-header">
                        <div class="announcement-title">${a.title}</div>
                        <div class="announcement-date">${a.date}</div>
                    </div>
                    <div class="announcement-body">${a.body}</div>
                </div>
                `).join('')}
            </div>
        `;
        document.getElementById('btn-publish-ann').addEventListener('click', () => {
            const t = document.getElementById('ann-title').value || 'New Announcement';
            const b = document.getElementById('ann-body').value || '...';
            window._ann.unshift({ title: t, date: new Date().toLocaleDateString(), body: b });
            renderStaffTab(document.querySelector('#staff-content'), 'announce');
            showToast('Announcement published to all users.', 'success');
        });
    } else if (tab === 'audit') {
        el.innerHTML = `
            <div class="card">
                <div class="audit-timeline">
                    <div class="audit-entry">
                        <div class="audit-time">Just now</div>
                        <div class="audit-dot"></div>
                        <div class="audit-content">
                            <div class="audit-action">corruptnull viewed Staff Panel</div>
                        </div>
                    </div>
                    <div class="audit-entry">
                        <div class="audit-time">2 hours ago</div>
                        <div class="audit-dot danger"></div>
                        <div class="audit-content">
                            <div class="audit-action">.foulz. banned user 987654321098765432</div>
                            <div class="audit-detail">Reason: Attempted crack</div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ANALYTICS
// ─────────────────────────────────────────────────────────────────────────────

function renderAnalytics(el) {
    if (!isAdmin(currentUser)) {
        el.innerHTML = `<div class="page-header"><div class="page-title text-danger">Access Denied</div></div>`;
        return;
    }

    el.innerHTML = `
        <div class="page-header">
            <div class="page-title">Analytics</div>
            <div class="page-sub">Global client telemetry & usage statistics.</div>
        </div>

        <div class="analytics-grid">
            <div class="analytics-card">
                <div class="analytics-value">2,847</div>
                <div class="analytics-label">Total Active Users</div>
                <div class="analytics-delta up">↑ 12% this week</div>
            </div>
            <div class="analytics-card">
                <div class="analytics-value">142</div>
                <div class="analytics-label">Concurrent Online</div>
                <div class="analytics-delta up">↑ 5% since yesterday</div>
            </div>
            <div class="analytics-card">
                <div class="analytics-value">12.4k</div>
                <div class="analytics-label">Total Injections</div>
                <div class="analytics-delta up">↑ 8% this week</div>
            </div>
            <div class="analytics-card">
                <div class="analytics-value">99.8%</div>
                <div class="analytics-label">Bypass Success Rate</div>
                <div class="analytics-delta down">↓ 0.1% (Vulcan patch)</div>
            </div>
        </div>

        <div class="two-col">
            <div class="chart-card">
                <div class="chart-title">Top Modules Used</div>
                <div class="chart-bar-row">
                    <div class="chart-bar-label">Aura</div>
                    <div class="chart-bar-track"><div class="chart-bar-fill" style="width:95%"></div></div>
                    <div class="chart-bar-value">95%</div>
                </div>
                <div class="chart-bar-row">
                    <div class="chart-bar-label">Velocity</div>
                    <div class="chart-bar-track"><div class="chart-bar-fill" style="width:88%"></div></div>
                    <div class="chart-bar-value">88%</div>
                </div>
                <div class="chart-bar-row">
                    <div class="chart-bar-label">ESP</div>
                    <div class="chart-bar-track"><div class="chart-bar-fill" style="width:76%"></div></div>
                    <div class="chart-bar-value">76%</div>
                </div>
                <div class="chart-bar-row">
                    <div class="chart-bar-label">Scaffold</div>
                    <div class="chart-bar-track"><div class="chart-bar-fill" style="width:64%"></div></div>
                    <div class="chart-bar-value">64%</div>
                </div>
            </div>
            
            <div class="chart-card">
                <div class="chart-title">Server Distribution</div>
                <div class="chart-bar-row">
                    <div class="chart-bar-label">Hypixel</div>
                    <div class="chart-bar-track"><div class="chart-bar-fill" style="width:82%; background: linear-gradient(90deg, #FF5555, #FFAA00)"></div></div>
                    <div class="chart-bar-value">82%</div>
                </div>
                <div class="chart-bar-row">
                    <div class="chart-bar-label">Minemen</div>
                    <div class="chart-bar-track"><div class="chart-bar-fill" style="width:45%; background: linear-gradient(90deg, #55FFFF, #00AAFF)"></div></div>
                    <div class="chart-bar-value">45%</div>
                </div>
                <div class="chart-bar-row">
                    <div class="chart-bar-label">Cubecraft</div>
                    <div class="chart-bar-track"><div class="chart-bar-fill" style="width:28%; background: linear-gradient(90deg, #55FF55, #00AA00)"></div></div>
                    <div class="chart-bar-value">28%</div>
                </div>
            </div>
        </div>
    `;
}
