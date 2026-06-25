'use strict';

const POLL_MS = 5000;

const el = {
    connStatus: document.getElementById('conn-status'),
    connText  : document.getElementById('conn-text'),
    modCount  : document.getElementById('stat-modcount'),
    toggles   : document.getElementById('stat-toggles'),
    groups    : document.getElementById('module-groups'),
};

function setConn(live) {
    el.connStatus.className = 'status ' + (live ? 'live' : 'down');
    el.connText.textContent = live ? 'Live' : 'Disconnected';
}

function escapeHtml(s) {
    return String(s).replace(/[&<>"']/g, (c) => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
    }[c]));
}

function renderGroups(modules) {
    if (!modules.length) {
        el.groups.innerHTML = '<div class="kv-list empty">No modules in the catalog</div>';
        return;
    }

    const byCategory = new Map();
    for (const m of modules) {
        if (!byCategory.has(m.category)) byCategory.set(m.category, []);
        byCategory.get(m.category).push(m);
    }

    el.groups.innerHTML = [...byCategory.entries()].map(([category, mods]) => `
        <div class="panel module-group">
            <div class="panel-head"><h2>${escapeHtml(category)}</h2></div>
            ${mods.map(m => `
                <div class="module-card">
                    <div class="module-card-head">
                        <span class="module-name">${escapeHtml(m.name)}</span>
                        <span class="module-uses">${m.uses.toLocaleString()} use${m.uses === 1 ? '' : 's'}</span>
                    </div>
                    <div class="module-desc">${escapeHtml(m.description)}</div>
                    <div class="module-bar-track">
                        <div class="module-bar-fill" style="width:${Math.min(100, m.share)}%"></div>
                    </div>
                </div>
            `).join('')}
        </div>
    `).join('');
}

let lastError = false;

async function poll() {
    try {
        const res = await fetch('/api/modules', { cache: 'no-store' });
        if (!res.ok) throw new Error('bad status');
        const data = await res.json();

        setConn(true);
        lastError = false;

        el.modCount.textContent = data.totalModules;
        el.toggles.textContent  = data.totalToggles.toLocaleString();
        renderGroups(data.modules);
    } catch (err) {
        if (!lastError) setConn(false);
        lastError = true;
    }
}

poll();
setInterval(poll, POLL_MS);
