'use strict';

/**
 * Quark Global Chat — real-time relay server.
 *
 * A small, dependency-light WebSocket relay that powers the launcher's Global
 * Chat page. It keeps an accurate online count, broadcasts messages to every
 * connected client, replays recent history to newcomers, and applies basic
 * rate-limiting and message hygiene. No database is required — recent history
 * lives in an in-memory ring buffer.
 *
 * Run:   PORT=8787 node chat-relay.js
 * Deploy anywhere that can hold a long-lived WebSocket (a VPS, Fly.io,
 * Railway, Render, etc.). Then point the launcher at it:
 *   Settings → Global Chat → Relay URL  →  wss://your-host
 *
 * Protocol (JSON text frames)
 *   client → server: { type:'join', user:'Name' }
 *                     { type:'chat', text:'hello' }
 *   server → client: { type:'welcome', online:N, you:'Name' }
 *                     { type:'history', messages:[ {user,text,ts}... ] }
 *                     { type:'presence', online:N }
 *                     { type:'msg', user, text, ts }
 *                     { type:'system', text, ts }
 *                     { type:'error', text }
 */

const http = require('http');
const { WebSocketServer } = require('ws');

const PORT          = parseInt(process.env.PORT || '8787', 10);
const MAX_LEN       = 240;          // max characters per message
const HISTORY_SIZE  = 50;           // recent messages replayed to new clients
const RATE_WINDOW   = 10_000;       // ms
const RATE_MAX      = 8;            // messages per window per connection
const NAME_MAX      = 24;
const HEARTBEAT_MS  = 30_000;

const history = [];                 // ring buffer of {user,text,ts}

function now() { return Date.now(); }

function sanitize(str, max) {
    return String(str == null ? '' : str)
        .replace(/[\u0000-\u001f\u007f]/g, '') // strip control chars
        .trim()
        .slice(0, max);
}

function pushHistory(entry) {
    history.push(entry);
    if (history.length > HISTORY_SIZE) history.shift();
}

// A bare HTTP server so the same URL answers health checks (and platform probes).
const server = http.createServer((req, res) => {
    if (req.url === '/health' || req.url === '/') {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ ok: true, online: countOnline(), uptime: process.uptime() }));
        return;
    }
    res.writeHead(404);
    res.end();
});

const wss = new WebSocketServer({ server });

function countOnline() {
    let n = 0;
    for (const c of wss.clients) if (c.joined) n++;
    return n;
}

function broadcast(obj, predicate) {
    const data = JSON.stringify(obj);
    for (const c of wss.clients) {
        if (c.readyState === c.OPEN && c.joined && (!predicate || predicate(c))) {
            try { c.send(data); } catch (_) {}
        }
    }
}

function send(ws, obj) {
    if (ws.readyState === ws.OPEN) {
        try { ws.send(JSON.stringify(obj)); } catch (_) {}
    }
}

wss.on('connection', (ws) => {
    ws.isAlive = true;
    ws.joined  = false;
    ws.user    = null;
    ws.stamps  = [];

    ws.on('pong', () => { ws.isAlive = true; });

    ws.on('message', (raw) => {
        let msg;
        try { msg = JSON.parse(raw.toString()); } catch (_) { return; }
        if (!msg || typeof msg.type !== 'string') return;

        if (msg.type === 'join') {
            if (ws.joined) return;
            const name = sanitize(msg.user, NAME_MAX) || 'Guest';
            ws.user   = name;
            ws.joined = true;
            send(ws, { type: 'welcome', online: countOnline(), you: name });
            send(ws, { type: 'history', messages: history.slice() });
            broadcast({ type: 'presence', online: countOnline() });
            broadcast({ type: 'system', text: `${name} joined`, ts: now() }, c => c !== ws);
            return;
        }

        if (!ws.joined) return;

        if (msg.type === 'chat') {
            const text = sanitize(msg.text, MAX_LEN);
            if (!text) return;

            // Per-connection rate limit.
            const t = now();
            ws.stamps = ws.stamps.filter(s => t - s < RATE_WINDOW);
            if (ws.stamps.length >= RATE_MAX) {
                send(ws, { type: 'error', text: 'You are sending messages too fast.' });
                return;
            }
            ws.stamps.push(t);

            const entry = { user: ws.user, text, ts: t };
            pushHistory(entry);
            broadcast({ type: 'msg', ...entry });
        }
    });

    ws.on('close', () => {
        if (ws.joined) {
            broadcast({ type: 'presence', online: countOnline() });
            broadcast({ type: 'system', text: `${ws.user} left`, ts: now() });
        }
    });

    ws.on('error', () => {});
});

// Drop sockets that stop answering heartbeats.
const heartbeat = setInterval(() => {
    for (const ws of wss.clients) {
        if (ws.isAlive === false) { ws.terminate(); continue; }
        ws.isAlive = false;
        try { ws.ping(); } catch (_) {}
    }
}, HEARTBEAT_MS);

wss.on('close', () => clearInterval(heartbeat));

server.listen(PORT, () => {
    console.log(`[chat-relay] listening on :${PORT} (ws + http /health)`);
});
