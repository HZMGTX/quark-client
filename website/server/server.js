'use strict';

/**
 * Quark Stats — telemetry backend for the dashboard.
 *
 * Receives real activity events from the launcher (Electron app) and the
 * injected Java client, aggregates them in memory, persists the aggregate
 * to disk so counts survive a restart, and serves the dashboard's static
 * files plus its read API from the same port.
 *
 * No database, no npm dependencies — just Node core modules.
 *
 * Endpoints
 *   GET  /health              -> { ok, uptime }
 *   GET  /api/stats           -> aggregate snapshot for the dashboard
 *   GET  /api/modules         -> the real module catalog + live usage counts
 *   POST /api/event           -> record one real event
 *                                 { source:'launcher'|'client', type, clientId, payload? }
 *   GET  /*                   -> static files from ../public
 *
 * Run:   PORT=8788 node server.js
 */

const http = require('http');
const fs   = require('fs');
const path = require('path');

const PORT          = parseInt(process.env.PORT || '8788', 10);
const PUBLIC_DIR     = path.join(__dirname, '..', 'public');
const DATA_DIR        = path.join(__dirname, 'data');
const DATA_FILE        = path.join(DATA_DIR, 'stats.json');

const RECENT_MAX     = 100;   // events kept verbatim for the activity feed
const SERIES_MINUTES  = 60;   // minutes of per-minute history kept for the chart
const ONLINE_WINDOW_MS = 120_000; // a client counts as "online" if seen in the last 2 min
const SAVE_INTERVAL_MS = 10_000;  // flush aggregate state to disk at most this often

const RATE_WINDOW_MS = 60_000;
const RATE_MAX        = 120;   // events per IP per window
const MAX_TYPE_LEN     = 64;
const MAX_SOURCE_LEN   = 16;
const MAX_CLIENTID_LEN = 64;
const MAX_PAYLOAD_BYTES = 2048;
const MAX_BODY_BYTES    = 8192;

const VALID_SOURCES = new Set(['launcher', 'client']);

// Mirrors launcher/agent/StandaloneClient.java's buildModules() — the actual
// module list shipped in the client, not a marketing count. Kept in sync by
// hand since the Java side has no build step that exports this as data.
const MODULE_CATALOG = [
    { category: 'Render', name: 'FullBright',    description: 'Maxes out brightness while enabled' },
    { category: 'Render', name: 'Zoom',          description: 'Hold C to zoom the camera in' },
    { category: 'HUD',    name: 'Watermark',     description: 'Quark logo + FPS' },
    { category: 'HUD',    name: 'ModuleList',    description: 'Active module list' },
    { category: 'HUD',    name: 'FPS',           description: 'Standalone FPS counter' },
    { category: 'HUD',    name: 'Keystrokes',    description: 'WASD + mouse keys with live CPS' },
    { category: 'HUD',    name: 'Coordinates',   description: 'Live player XYZ position' },
    { category: 'HUD',    name: 'ArmorStatus',   description: 'Worn armor + durability' },
    { category: 'HUD',    name: 'Ping',          description: 'Live connection latency' },
    { category: 'HUD',    name: 'Direction',     description: 'Facing direction + yaw' },
    { category: 'HUD',    name: 'Clock',         description: 'Real-time system clock' },
    { category: 'Misc',   name: 'ClickGui',      description: 'This menu' },
    { category: 'Misc',   name: 'ConfigManager', description: 'Autosaves your settings' },
    { category: 'Misc',   name: 'Notifications', description: 'Toast pop-ups for toggles' },
];

function now() { return Date.now(); }

// ── Aggregate state ─────────────────────────────────────────────────────────

const state = {
    startedAt: now(),
    totalEvents: 0,
    bySource: { launcher: 0, client: 0 },
    byType: {},
    byModule: {},
    recent: [],                 // ring buffer, newest first
    seriesByMinute: {},          // minuteTs(string) -> count
    knownClients: {},             // clientId -> lastSeen ts
};

let dirty = false;

function loadState() {
    try {
        const raw = fs.readFileSync(DATA_FILE, 'utf8');
        const saved = JSON.parse(raw);
        Object.assign(state, {
            startedAt: typeof saved.startedAt === 'number' ? saved.startedAt : now(),
            totalEvents: saved.totalEvents || 0,
            bySource: { launcher: 0, client: 0, ...saved.bySource },
            byType: saved.byType || {},
            byModule: saved.byModule || {},
            recent: Array.isArray(saved.recent) ? saved.recent.slice(0, RECENT_MAX) : [],
            seriesByMinute: saved.seriesByMinute || {},
            knownClients: saved.knownClients || {},
        });
    } catch (_) {
        // No prior state (first run) — start fresh.
    }
}

function saveState() {
    if (!dirty) return;
    dirty = false;
    try {
        fs.mkdirSync(DATA_DIR, { recursive: true });
        fs.writeFileSync(DATA_FILE, JSON.stringify(state));
    } catch (_) {}
}

function pruneSeries() {
    const cutoff = now() - SERIES_MINUTES * 60_000;
    for (const key of Object.keys(state.seriesByMinute)) {
        if (parseInt(key, 10) < cutoff) delete state.seriesByMinute[key];
    }
}

function pruneClients() {
    const cutoff = now() - 30 * 24 * 60 * 60_000; // forget clients silent for 30 days
    for (const id of Object.keys(state.knownClients)) {
        if (state.knownClients[id] < cutoff) delete state.knownClients[id];
    }
}

function recordEvent({ source, type, clientId, payload }) {
    const t = now();
    state.totalEvents++;
    state.bySource[source] = (state.bySource[source] || 0) + 1;
    state.byType[type] = (state.byType[type] || 0) + 1;

    if (type === 'module_toggle' && payload && typeof payload.module === 'string') {
        const mod = payload.module.slice(0, 32);
        state.byModule[mod] = (state.byModule[mod] || 0) + 1;
    }

    if (clientId) state.knownClients[clientId] = t;

    const minuteKey = String(Math.floor(t / 60_000) * 60_000);
    state.seriesByMinute[minuteKey] = (state.seriesByMinute[minuteKey] || 0) + 1;

    state.recent.unshift({ source, type, payload: payload || null, ts: t });
    if (state.recent.length > RECENT_MAX) state.recent.length = RECENT_MAX;

    dirty = true;
}

function buildSnapshot() {
    pruneSeries();
    const t = now();
    let online = 0;
    for (const id of Object.keys(state.knownClients)) {
        if (t - state.knownClients[id] < ONLINE_WINDOW_MS) online++;
    }

    const series = Object.keys(state.seriesByMinute)
        .map(k => ({ minute: parseInt(k, 10), count: state.seriesByMinute[k] }))
        .sort((a, b) => a.minute - b.minute);

    const topModules = Object.entries(state.byModule)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 12)
        .map(([name, count]) => ({ name, count }));

    return {
        ok: true,
        uptime: Math.floor((t - state.startedAt) / 1000),
        startedAt: state.startedAt,
        totalEvents: state.totalEvents,
        uniqueClients: Object.keys(state.knownClients).length,
        online,
        bySource: state.bySource,
        byType: state.byType,
        topModules,
        recent: state.recent.slice(0, 50),
        series,
    };
}

// ── Request helpers ─────────────────────────────────────────────────────────

function sendJson(res, code, obj) {
    const body = JSON.stringify(obj);
    res.writeHead(code, {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(body),
        'Access-Control-Allow-Origin': '*',
    });
    res.end(body);
}

function readBody(req, maxBytes) {
    return new Promise((resolve, reject) => {
        let size = 0;
        const chunks = [];
        req.on('data', (chunk) => {
            size += chunk.length;
            if (size > maxBytes) {
                reject(new Error('payload too large'));
                req.destroy();
                return;
            }
            chunks.push(chunk);
        });
        req.on('end', () => resolve(Buffer.concat(chunks).toString('utf8')));
        req.on('error', reject);
    });
}

function clientIp(req) {
    return (req.socket && req.socket.remoteAddress) || 'unknown';
}

const rateMap = new Map(); // ip -> timestamps[]

function rateLimited(ip) {
    const t = now();
    const stamps = (rateMap.get(ip) || []).filter(s => t - s < RATE_WINDOW_MS);
    stamps.push(t);
    rateMap.set(ip, stamps);
    return stamps.length > RATE_MAX;
}

function sanitizeStr(v, max) {
    return String(v == null ? '' : v).slice(0, max);
}

// ── Static file serving ─────────────────────────────────────────────────────

const MIME = {
    '.html': 'text/html; charset=utf-8',
    '.js'  : 'text/javascript; charset=utf-8',
    '.css' : 'text/css; charset=utf-8',
    '.json': 'application/json',
    '.svg' : 'image/svg+xml',
    '.png' : 'image/png',
    '.ico' : 'image/x-icon',
};

function serveStatic(req, res) {
    let reqPath = decodeURIComponent(req.url.split('?')[0]);
    if (reqPath === '/') reqPath = '/index.html';

    const resolved = path.normalize(path.join(PUBLIC_DIR, reqPath));
    if (!resolved.startsWith(PUBLIC_DIR)) {
        res.writeHead(403); res.end(); return;
    }

    fs.readFile(resolved, (err, data) => {
        if (err) { res.writeHead(404); res.end('Not found'); return; }
        const ext = path.extname(resolved);
        res.writeHead(200, { 'Content-Type': MIME[ext] || 'application/octet-stream' });
        res.end(data);
    });
}

// ── Server ───────────────────────────────────────────────────────────────────

loadState();

const server = http.createServer(async (req, res) => {
    const url = req.url.split('?')[0];

    if (req.method === 'OPTIONS') {
        res.writeHead(204, {
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Methods': 'GET,POST,OPTIONS',
            'Access-Control-Allow-Headers': 'Content-Type',
        });
        res.end();
        return;
    }

    if (url === '/health' && req.method === 'GET') {
        sendJson(res, 200, { ok: true, uptime: Math.floor((now() - state.startedAt) / 1000) });
        return;
    }

    if (url === '/api/stats' && req.method === 'GET') {
        sendJson(res, 200, buildSnapshot());
        return;
    }

    if (url === '/api/modules' && req.method === 'GET') {
        const totalToggles = Object.values(state.byModule).reduce((a, b) => a + b, 0);
        const modules = MODULE_CATALOG.map(m => {
            const uses = state.byModule[m.name] || 0;
            return {
                ...m,
                uses,
                share: totalToggles ? Math.round((uses / totalToggles) * 1000) / 10 : 0,
            };
        });
        sendJson(res, 200, { ok: true, totalModules: modules.length, totalToggles, modules });
        return;
    }

    if (url === '/api/event' && req.method === 'POST') {
        const ip = clientIp(req);
        if (rateLimited(ip)) {
            sendJson(res, 429, { ok: false, error: 'rate limited' });
            return;
        }
        try {
            const raw = await readBody(req, MAX_BODY_BYTES);
            const msg = JSON.parse(raw || '{}');

            const source = sanitizeStr(msg.source, MAX_SOURCE_LEN);
            const type   = sanitizeStr(msg.type, MAX_TYPE_LEN);
            const clientId = sanitizeStr(msg.clientId, MAX_CLIENTID_LEN) || null;

            if (!VALID_SOURCES.has(source) || !type) {
                sendJson(res, 400, { ok: false, error: 'invalid source/type' });
                return;
            }

            let payload = null;
            if (msg.payload && typeof msg.payload === 'object') {
                const encoded = JSON.stringify(msg.payload);
                if (encoded.length <= MAX_PAYLOAD_BYTES) payload = msg.payload;
            }

            recordEvent({ source, type, clientId, payload });
            sendJson(res, 200, { ok: true });
        } catch (_) {
            sendJson(res, 400, { ok: false, error: 'bad request' });
        }
        return;
    }

    if (req.method === 'GET') {
        serveStatic(req, res);
        return;
    }

    res.writeHead(404);
    res.end();
});

setInterval(() => { saveState(); pruneClients(); }, SAVE_INTERVAL_MS);

process.on('SIGINT',  () => { saveState(); process.exit(0); });
process.on('SIGTERM', () => { saveState(); process.exit(0); });

server.listen(PORT, () => {
    console.log(`[quark-stats] listening on :${PORT} (dashboard + api)`);
});
