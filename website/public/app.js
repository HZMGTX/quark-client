'use strict';

const POLL_MS = 5000;

const el = {
    connStatus: document.getElementById('conn-status'),
    connText  : document.getElementById('conn-text'),
    online    : document.getElementById('stat-online'),
    total     : document.getElementById('stat-total'),
    clients   : document.getElementById('stat-clients'),
    uptime    : document.getElementById('stat-uptime'),
    bySource  : document.getElementById('by-source'),
    byType    : document.getElementById('by-type'),
    topModules: document.getElementById('top-modules'),
    feed      : document.getElementById('recent-feed'),
    chart     : document.getElementById('chart'),
};

function setConn(live) {
    el.connStatus.className = 'status ' + (live ? 'live' : 'down');
    el.connText.textContent = live ? 'Live' : 'Disconnected';
}

function fmtUptime(seconds) {
    const d = Math.floor(seconds / 86400);
    const h = Math.floor((seconds % 86400) / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    if (d > 0) return `${d}d ${h}h`;
    if (h > 0) return `${h}h ${m}m`;
    return `${m}m`;
}

function fmtTime(ts) {
    const dt = new Date(ts);
    return dt.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
}

function renderKv(container, entries, emptyText) {
    if (!entries.length) {
        container.className = 'kv-list empty';
        container.textContent = emptyText;
        return;
    }
    container.className = 'kv-list';
    container.innerHTML = entries
        .map(([name, count]) => `
            <div class="kv-row">
                <span class="kv-name">${escapeHtml(name)}</span>
                <span class="kv-count">${count}</span>
            </div>
        `).join('');
}

function escapeHtml(s) {
    return String(s).replace(/[&<>"']/g, (c) => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
    }[c]));
}

function renderFeed(recent) {
    if (!recent.length) {
        el.feed.className = 'feed empty';
        el.feed.textContent = 'No events yet';
        return;
    }
    el.feed.className = 'feed';
    el.feed.innerHTML = recent.map(ev => {
        const payloadText = ev.payload ? escapeHtml(JSON.stringify(ev.payload)) : '';
        return `
            <div class="feed-row">
                <span class="feed-time">${fmtTime(ev.ts)}</span>
                <span class="feed-source ${ev.source}">${escapeHtml(ev.source)}</span>
                <span class="feed-type">${escapeHtml(ev.type)}</span>
                <span class="feed-payload">${payloadText}</span>
            </div>
        `;
    }).join('');
}

function renderChart(series) {
    const ctx = el.chart.getContext('2d');
    const w = el.chart.width, h = el.chart.height;
    ctx.clearRect(0, 0, w, h);

    if (!series.length) {
        ctx.fillStyle = '#6B6B8A';
        ctx.font = '13px sans-serif';
        ctx.fillText('No activity recorded yet', 16, h / 2);
        return;
    }

    const max = Math.max(1, ...series.map(p => p.count));
    const barW = w / Math.max(60, series.length);
    const grad = ctx.createLinearGradient(0, 0, 0, h);
    grad.addColorStop(0, '#A855F7');
    grad.addColorStop(1, '#06B6D4');
    ctx.fillStyle = grad;

    const offset = Math.max(0, 60 - series.length);
    series.forEach((p, i) => {
        const x = (offset + i) * barW;
        const barH = (p.count / max) * (h - 24);
        ctx.fillRect(x + 1, h - barH - 4, Math.max(1, barW - 2), barH);
    });
}

let lastError = false;

async function poll() {
    try {
        const res = await fetch('/api/stats', { cache: 'no-store' });
        if (!res.ok) throw new Error('bad status');
        const data = await res.json();

        setConn(true);
        lastError = false;

        el.online.textContent   = data.online;
        el.total.textContent    = data.totalEvents.toLocaleString();
        el.clients.textContent  = data.uniqueClients.toLocaleString();
        el.uptime.textContent   = fmtUptime(data.uptime);

        renderKv(el.bySource, Object.entries(data.bySource).filter(([, c]) => c > 0), 'No data yet');
        renderKv(el.byType, Object.entries(data.byType).sort((a, b) => b[1] - a[1]).slice(0, 12), 'No data yet');
        renderKv(el.topModules, data.topModules.map(m => [m.name, m.count]), 'No module activity reported yet');
        renderFeed(data.recent);
        renderChart(data.series);
    } catch (err) {
        if (!lastError) setConn(false);
        lastError = true;
    }
}

poll();
setInterval(poll, POLL_MS);
