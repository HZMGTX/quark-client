'use strict';

// ─────────────────────────────────────────────────────────────────────────────
// Staff Role System
// ─────────────────────────────────────────────────────────────────────────────

const STAFF_IDS = {
    '1401853518100303932': 'Owner',
};

function getRole(user) {
    if (!user || user.guest) return 'User';
    return STAFF_IDS[user.id] || 'User';
}
function isStaff(user)   { return getRole(user) !== 'User'; }
function isAdmin(user)   { return ['Owner','Admin'].includes(getRole(user)); }
function isOwner(user)   { return getRole(user) === 'Owner'; }
function roleBadge(role) {
    return `<span class="role-badge role-${role.toLowerCase()}">${role}</span>`;
}

// ─────────────────────────────────────────────────────────────────────────────
// Staff Panel Data
// ─────────────────────────────────────────────────────────────────────────────

const STAFF_MEMBERS = [
    { name: 'corruptnull', id: '1401853518100303932', role: 'Owner',  status: 'online',  joined: 'Jan 2025' },
    { name: 'ModuleBot',   id: '0000000000000000001', role: 'Admin',  status: 'online',  joined: 'Feb 2025' },
    { name: 'GhostDev',    id: '0000000000000000002', role: 'Moderator', status: 'away', joined: 'Mar 2025' },
    { name: 'SupportUser', id: '0000000000000000003', role: 'Support', status: 'offline', joined: 'Apr 2025' },
];

const BAN_LIST = [
    { name: 'xCheater99',   hwid: 'A3F2...D8C1', reason: 'Leaked client',       date: '2025-06-01', by: 'corruptnull' },
    { name: 'SkidMaster',   hwid: 'B7E1...44A2', reason: 'Reselling keys',      date: '2025-05-28', by: 'corruptnull' },
    { name: 'TokenGrabber', hwid: 'C9D4...11F3', reason: 'Malware distribution', date: '2025-05-15', by: 'ModuleBot' },
];

const LICENSE_KEYS = [
    { key: 'QRK-A1B2-C3D4-E5F6', user: 'Player001', status: 'active',  expires: '2026-01-01' },
    { key: 'QRK-G7H8-I9J0-K1L2', user: 'Player002', status: 'active',  expires: '2025-12-15' },
    { key: 'QRK-M3N4-O5P6-Q7R8', user: 'xCheater99', status: 'revoked', expires: '2025-06-01' },
    { key: 'QRK-S9T0-U1V2-W3X4', user: 'Player004', status: 'expired', expires: '2025-05-01' },
    { key: 'QRK-Y5Z6-A7B8-C9D0', user: 'Player005', status: 'active',  expires: '2026-03-20' },
    { key: 'QRK-E1F2-G3H4-I5J6', user: '',           status: 'active',  expires: '2026-06-01' },
];

const ANNOUNCEMENTS = [
    { title: 'v1.0.0 Official Launch', body: '1000+ modules shipped. True XRay, Full ESP suite, Ghost Mode bypasses for GrimAC, Vulcan, and Polar.', date: 'Jun 4, 2025', priority: 'high' },
    { title: 'Anti-Cheat Bypass Update', body: 'GrimAC prediction bypass improved. Vulcan strafe and inventory move now fully bypassed on strict config.', date: 'May 20, 2025', priority: 'medium' },
    { title: 'Launcher Released', body: 'Electron desktop app with Discord OAuth2, process scanner, and NSIS installer.', date: 'May 10, 2025', priority: 'normal' },
];

const AUDIT_LOG = [
    { time: '06/04 18:00', action: 'Key Generated', detail: 'QRK-E1F2-G3H4-I5J6 (unassigned)', type: 'success' },
    { time: '06/04 17:30', action: 'Announcement Posted', detail: 'v1.0.0 Official Launch', type: '' },
    { time: '06/01 12:44', action: 'User Banned', detail: 'xCheater99 — Leaked client', type: 'danger' },
    { time: '05/28 09:12', action: 'User Banned', detail: 'SkidMaster — Reselling keys', type: 'danger' },
    { time: '05/28 09:13', action: 'Key Revoked', detail: 'QRK-M3N4-O5P6-Q7R8 (xCheater99 -> SkidMaster link)', type: 'warn' },
    { time: '05/20 14:00', action: 'Config Updated', detail: 'Anti-cheat bypass module settings pushed', type: '' },
    { time: '05/15 08:30', action: 'User Banned', detail: 'TokenGrabber — Malware distribution', type: 'danger' },
    { time: '05/10 10:00', action: 'Launcher Published', detail: 'v1.0.0 NSIS + portable builds pushed to dist/', type: 'success' },
];

// ─────────────────────────────────────────────────────────────────────────────
// Staff Panel Page
// ─────────────────────────────────────────────────────────────────────────────

let staffTab = 'users';

function renderStaffPanel(el) {
    el.innerHTML = `
        <div class="page-header">
            <div class="page-title">Staff Panel ${roleBadge(getRole(currentUser))}</div>
            <div class="page-sub">Manage users, bans, licenses, and announcements.</div>
        </div>
        <div class="staff-tabs mt-16">
            <button class="staff-tab ${staffTab==='users'?'active':''}" data-tab="users">Users</button>
            <button class="staff-tab ${staffTab==='bans'?'active':''}" data-tab="bans">Bans</button>
            <button class="staff-tab ${staffTab==='keys'?'active':''}" data-tab="keys">License Keys</button>
            <button class="staff-tab ${staffTab==='announce'?'active':''}" data-tab="announce">Announcements</button>
            <button class="staff-tab ${staffTab==='audit'?'active':''}" data-tab="audit">Audit Log</button>
        </div>
        <div id="staff-content"></div>`;

    el.querySelectorAll('.staff-tab').forEach(btn => {
        btn.addEventListener('click', () => { staffTab = btn.dataset.tab; renderStaffPanel(el); });
    });

    const c = el.querySelector('#staff-content');
    switch (staffTab) {
        case 'users':   renderStaffUsers(c);   break;
        case 'bans':    renderStaffBans(c);    break;
        case 'keys':    renderStaffKeys(c);    break;
        case 'announce':renderStaffAnnounce(c);break;
        case 'audit':   renderStaffAudit(c);   break;
    }
}

function renderStaffUsers(c) {
    c.innerHTML = `
        <div class="card">
            <div class="flex-row mb-12">
                <div style="flex:1"><div style="font-size:14px;font-weight:700">Staff Members</div>
                <div class="text-sm text-muted">${STAFF_MEMBERS.length} team members</div></div>
                ${isOwner(currentUser)?'<button class="btn btn-primary" id="add-staff-btn">+ Add Staff</button>':''}
            </div>
            <table class="staff-table">
                <tr><th>User</th><th>Role</th><th>Status</th><th>Joined</th></tr>
                ${STAFF_MEMBERS.map(m => `<tr>
                    <td style="font-weight:600">${esc(m.name)}</td>
                    <td>${roleBadge(m.role)}</td>
                    <td><span class="status-dot status-${m.status}"></span>${m.status}</td>
                    <td class="text-muted">${m.joined}</td>
                </tr>`).join('')}
            </table>
        </div>`;
}

function renderStaffBans(c) {
    c.innerHTML = `
        <div class="card">
            <div class="flex-row mb-12">
                <div style="flex:1"><div style="font-size:14px;font-weight:700">Ban Manager</div>
                <div class="text-sm text-muted">${BAN_LIST.length} banned users</div></div>
                <button class="btn btn-danger" id="add-ban-btn">+ Add Ban</button>
            </div>
            <div class="search-bar">
                <input class="text-input" placeholder="Search bans..." style="flex:1">
            </div>
            ${BAN_LIST.map(b => `<div class="ban-row">
                <div class="ban-icon">&#x26D4;</div>
                <div class="ban-info">
                    <div class="ban-name">${esc(b.name)} <span class="text-muted text-sm">HWID: ${b.hwid}</span></div>
                    <div class="ban-meta">${esc(b.reason)} &mdash; banned by ${esc(b.by)} on ${b.date}</div>
                </div>
                <div class="ban-actions">
                    <button class="btn btn-secondary" style="height:30px;font-size:11px;padding:0 12px">Unban</button>
                </div>
            </div>`).join('')}
        </div>`;
}

function renderStaffKeys(c) {
    c.innerHTML = `
        <div class="card mb-16">
            <div class="flex-row mb-12">
                <div style="flex:1"><div style="font-size:14px;font-weight:700">License Keys</div>
                <div class="text-sm text-muted">${LICENSE_KEYS.filter(k=>k.status==='active').length} active / ${LICENSE_KEYS.length} total</div></div>
                <button class="btn btn-primary" id="gen-key-btn">Generate Key</button>
            </div>
        </div>
        <div class="key-grid">
            ${LICENSE_KEYS.map(k => `<div class="key-card">
                <div class="key-value">${k.key}</div>
                <div class="key-meta">
                    User: <strong>${k.user||'Unassigned'}</strong><br>
                    Status: <span class="key-status-${k.status}">${k.status.toUpperCase()}</span><br>
                    Expires: ${k.expires}
                </div>
            </div>`).join('')}
        </div>`;
}

function renderStaffAnnounce(c) {
    c.innerHTML = `
        <div class="card mb-16">
            <div style="font-size:14px;font-weight:700;margin-bottom:12px">Post Announcement</div>
            <input class="text-input full mb-8" placeholder="Title..." id="ann-title">
            <div class="compose-area"><textarea placeholder="Write your announcement..." id="ann-body"></textarea></div>
            <div class="flex-row">
                <select class="text-input" id="ann-priority" style="width:140px">
                    <option value="normal">Normal</option>
                    <option value="medium">Medium</option>
                    <option value="high">High Priority</option>
                </select>
                <button class="btn btn-primary" id="post-ann-btn">Post</button>
            </div>
        </div>
        ${ANNOUNCEMENTS.map(a => {
            const cls = a.priority === 'high' ? ' announcement-priority-high' : a.priority === 'medium' ? ' announcement-priority-medium' : '';
            return `<div class="announcement-card${cls}">
                <div class="announcement-header">
                    <div class="announcement-title">${esc(a.title)}</div>
                    <div class="announcement-date">${a.date}</div>
                </div>
                <div class="announcement-body">${esc(a.body)}</div>
            </div>`;
        }).join('')}`;
}

function renderStaffAudit(c) {
    c.innerHTML = `
        <div class="card">
            <div style="font-size:14px;font-weight:700;margin-bottom:16px">Audit Log</div>
            <div class="audit-timeline">
                ${AUDIT_LOG.map(e => `<div class="audit-entry">
                    <div class="audit-time">${e.time}</div>
                    <div class="audit-dot ${e.type}"></div>
                    <div class="audit-content">
                        <div class="audit-action">${esc(e.action)}</div>
                        <div class="audit-detail">${esc(e.detail)}</div>
                    </div>
                </div>`).join('')}
            </div>
        </div>`;
}

// ─────────────────────────────────────────────────────────────────────────────
// Analytics Page
// ─────────────────────────────────────────────────────────────────────────────

function renderAnalytics(el) {
    el.innerHTML = `
        <div class="page-header">
            <div class="page-title">Analytics ${roleBadge(getRole(currentUser))}</div>
            <div class="page-sub">Usage statistics and module telemetry.</div>
        </div>

        <div class="analytics-grid mt-24">
            <div class="analytics-card"><div class="analytics-value">2,847</div><div class="analytics-label">Total Users</div><div class="analytics-delta up">+12.4%</div></div>
            <div class="analytics-card"><div class="analytics-value">142</div><div class="analytics-label">Active Today</div><div class="analytics-delta up">+8.2%</div></div>
            <div class="analytics-card"><div class="analytics-value">1,204</div><div class="analytics-label">Injections (7d)</div><div class="analytics-delta up">+23.1%</div></div>
            <div class="analytics-card"><div class="analytics-value">98.7%</div><div class="analytics-label">Uptime</div><div class="analytics-delta up">stable</div></div>
        </div>

        <div class="two-col">
            <div class="chart-card">
                <div class="chart-title">Top Modules (7 days)</div>
                ${chartBar('KillAura', 89)}
                ${chartBar('CrystalAura', 76)}
                ${chartBar('Speed', 71)}
                ${chartBar('XRay', 68)}
                ${chartBar('ESP', 64)}
                ${chartBar('Scaffold', 52)}
                ${chartBar('Flight', 41)}
                ${chartBar('AutoTotem', 38)}
            </div>
            <div class="chart-card">
                <div class="chart-title">AC Bypass Rates</div>
                ${chartBar('GrimAC', 94)}
                ${chartBar('Vulcan', 88)}
                ${chartBar('Polar', 91)}
                ${chartBar('Matrix', 97)}
                ${chartBar('NCP', 99)}
                ${chartBar('AAC', 95)}
                ${chartBar('Vanilla', 100)}
            </div>
        </div>

        <div class="chart-card">
            <div class="chart-title">Server Distribution</div>
            ${chartBar('Anarchy', 34)}
            ${chartBar('HVH', 28)}
            ${chartBar('Practice', 18)}
            ${chartBar('SMP / Survival', 12)}
            ${chartBar('Crystal PVP', 8)}
        </div>`;
}

function chartBar(label, pct) {
    return `<div class="chart-bar-row">
        <div class="chart-bar-label">${label}</div>
        <div class="chart-bar-track"><div class="chart-bar-fill" style="width:${pct}%"></div></div>
        <div class="chart-bar-value">${pct}%</div>
    </div>`;
}

// ─────────────────────────────────────────────────────────────────────────────
// Utility (local copy to avoid dependency on renderer.js load order)
// ─────────────────────────────────────────────────────────────────────────────

function esc(str) {
    if (!str) return '';
    return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}
