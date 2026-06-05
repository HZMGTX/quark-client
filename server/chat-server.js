'use strict';
const WebSocket = require('ws');
const http      = require('http');
const crypto    = require('crypto');

const PORT     = process.env.PORT || 8765;
const MAX_CLIENTS = 500;
const MSG_LIMIT   = 4096;   // max message size bytes

const server = http.createServer((req, res) => {
    if (req.url === '/health') {
        res.writeHead(200, { 'Content-Type': 'text/plain' });
        res.end('Quark Global Chat OK\n');
    } else {
        res.writeHead(404);
        res.end();
    }
});

const wss = new WebSocket.Server({ server });

const channels = new Map(); // channel -> Set<ws>
const clients  = new Map(); // ws -> { username, channel, id, joinedAt }

wss.on('connection', (ws, req) => {
    if (wss.clients.size > MAX_CLIENTS) {
        ws.close(1013, 'Server full');
        return;
    }

    const id = crypto.randomUUID();
    const ip = req.headers['x-forwarded-for'] || req.socket.remoteAddress;
    clients.set(ws, { username: null, channel: null, id, ip, joinedAt: Date.now() });

    ws.on('message', (data) => {
        if (data.length > MSG_LIMIT) return;

        let msg;
        try { msg = JSON.parse(data.toString()); } catch { return; }

        const client = clients.get(ws);
        if (!client) return;

        switch (msg.type) {
            case 'join': {
                const username = sanitize(msg.username, 32);
                const channel  = sanitize(msg.channel || 'Global', 32);
                if (!username) { ws.close(1008, 'Invalid username'); return; }

                // Leave old channel
                if (client.channel) leave(ws, client);

                client.username = username;
                client.channel  = channel;

                if (!channels.has(channel)) channels.set(channel, new Set());
                channels.get(channel).add(ws);

                broadcast(channel, {
                    type: 'system',
                    text: `§a${username} §7joined §b${channel}`,
                    count: channels.get(channel).size,
                }, ws);

                ws.send(JSON.stringify({
                    type: 'welcome',
                    channel,
                    id,
                    online: channels.get(channel).size,
                }));
                break;
            }

            case 'msg': {
                if (!client.username || !client.channel) return;
                const text = sanitize(msg.text, 256);
                if (!text) return;

                // Simple spam filter: no more than 3 messages per 2 seconds
                client._msgs = (client._msgs || []).filter(t => Date.now() - t < 2000);
                if (client._msgs.length >= 3) return;
                client._msgs.push(Date.now());

                broadcast(client.channel, {
                    type    : 'msg',
                    username: client.username,
                    text,
                    channel : client.channel,
                    id,
                });
                break;
            }

            case 'ping':
                ws.send(JSON.stringify({ type: 'pong', ts: Date.now() }));
                break;
        }
    });

    ws.on('close', () => {
        const client = clients.get(ws);
        if (client) leave(ws, client);
        clients.delete(ws);
    });

    ws.on('error', () => ws.terminate());
});

function leave(ws, client) {
    if (!client.channel) return;
    const ch = channels.get(client.channel);
    if (ch) {
        ch.delete(ws);
        if (ch.size === 0) channels.delete(client.channel);
        else broadcast(client.channel, {
            type: 'system',
            text: `§c${client.username} §7left §b${client.channel}`,
            count: ch.size,
        }, ws);
    }
}

function broadcast(channel, msg, exclude) {
    const ch = channels.get(channel);
    if (!ch) return;
    const raw = JSON.stringify(msg);
    for (const ws of ch) {
        if (ws !== exclude && ws.readyState === WebSocket.OPEN) {
            ws.send(raw);
        }
    }
}

function sanitize(str, maxLen) {
    if (typeof str !== 'string') return null;
    return str.replace(/[^\w\s\-.!?#@:'"()À-ɏ]/g, '').trim().slice(0, maxLen) || null;
}

server.listen(PORT, () => {
    console.log(`[Quark Chat Server] Listening on ws://0.0.0.0:${PORT}`);
    console.log(`[Quark Chat Server] Health check: http://localhost:${PORT}/health`);
});

// Graceful shutdown
process.on('SIGTERM', () => { wss.close(); server.close(); });
process.on('SIGINT',  () => { wss.close(); server.close(); process.exit(0); });
