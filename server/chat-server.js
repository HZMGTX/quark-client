'use strict';
const WebSocket = require('ws');
const http      = require('http');
const crypto    = require('crypto');

const PORT        = process.env.PORT || 8765;
const MAX_CLIENTS = 500;
const MSG_LIMIT   = 4096;   // max message size bytes
const HISTORY_MAX = 50;     // messages to keep per channel

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

const channels   = new Map(); // channel -> Set<ws>
const clients    = new Map(); // ws -> { username, channel, id, joinedAt, friends, status, activity }
const userIndex  = new Map(); // username -> ws  (for DMs / mentions)
const history    = new Map(); // channel -> Array of last HISTORY_MAX messages

// ---------------------------------------------------------------------------
// Emoji text replacements
// ---------------------------------------------------------------------------
const EMOJI_MAP = {
    ':smile:'        : '😊',
    ':laugh:'        : '😂',
    ':joy:'          : '😂',
    ':sad:'          : '😢',
    ':cry:'          : '😭',
    ':angry:'        : '😠',
    ':heart:'        : '❤️',
    ':thumbsup:'     : '👍',
    ':thumbsdown:'   : '👎',
    ':fire:'         : '🔥',
    ':star:'         : '⭐',
    ':warning:'      : '⚠️',
    ':check:'        : '✅',
    ':x:'            : '❌',
    ':wave:'         : '👋',
    ':clap:'         : '👏',
    ':skull:'        : '💀',
    ':sword:'        : '⚔️',
    ':shield:'       : '🛡️',
    ':diamond:'      : '💎',
    ':pickaxe:'      : '⛏️',
    ':creeper:'      : '💚',
    ':ghost:'        : '👻',
    ':rocket:'       : '🚀',
    ':crown:'        : '👑',
    ':gg:'           : '🎮',
    ':pog:'          : '😮',
    ':sus:'          : '📮',
    ':lol:'          : '😂',
    ':100:'          : '💯',
};

function applyEmojis(text) {
    return text.replace(/:[a-z0-9_]+:/g, (match) => EMOJI_MAP[match] || match);
}

// ---------------------------------------------------------------------------
// History helpers
// ---------------------------------------------------------------------------
function pushHistory(channel, msgObj) {
    if (!history.has(channel)) history.set(channel, []);
    const arr = history.get(channel);
    arr.push(msgObj);
    if (arr.length > HISTORY_MAX) arr.shift();
}

function getHistory(channel) {
    return history.get(channel) || [];
}

// ---------------------------------------------------------------------------
// WebSocket server
// ---------------------------------------------------------------------------
wss.on('connection', (ws, req) => {
    if (wss.clients.size > MAX_CLIENTS) {
        ws.close(1013, 'Server full');
        return;
    }

    const id = crypto.randomUUID();
    const ip = req.headers['x-forwarded-for'] || req.socket.remoteAddress;
    clients.set(ws, {
        username : null,
        channel  : null,
        id,
        ip,
        joinedAt : Date.now(),
        friends  : new Set(),   // set of usernames this client added
        status   : 'online',
        activity : '',
        _msgs    : [],
    });

    ws.on('message', (data) => {
        if (data.length > MSG_LIMIT) return;

        let msg;
        try { msg = JSON.parse(data.toString()); } catch { return; }

        const client = clients.get(ws);
        if (!client) return;

        switch (msg.type) {

            // ----------------------------------------------------------------
            // join
            // ----------------------------------------------------------------
            case 'join': {
                const username = sanitize(msg.username, 32);
                const channel  = sanitize(msg.channel || 'Global', 32);
                if (!username) { ws.close(1008, 'Invalid username'); return; }

                // Leave old channel / unindex old username
                if (client.channel) leave(ws, client);
                if (client.username) userIndex.delete(client.username);

                client.username = username;
                client.channel  = channel;
                userIndex.set(username, ws);

                if (!channels.has(channel)) channels.set(channel, new Set());
                channels.get(channel).add(ws);

                broadcast(channel, {
                    type    : 'system',
                    text    : `§a${username} §7joined §b${channel}`,
                    count   : channels.get(channel).size,
                    username,
                    event   : 'join',
                }, ws);

                // Send welcome + history
                ws.send(JSON.stringify({
                    type   : 'welcome',
                    channel,
                    id,
                    online : channels.get(channel).size,
                }));

                const hist = getHistory(channel);
                if (hist.length > 0) {
                    ws.send(JSON.stringify({ type: 'history', channel, messages: hist }));
                }
                break;
            }

            // ----------------------------------------------------------------
            // msg
            // ----------------------------------------------------------------
            case 'msg': {
                if (!client.username || !client.channel) return;
                let text = sanitize(msg.text, 256);
                if (!text) return;

                // Rate-limit: no more than 3 messages per 2 seconds
                client._msgs = client._msgs.filter(t => Date.now() - t < 2000);
                if (client._msgs.length >= 3) return;
                client._msgs.push(Date.now());

                // Emoji replacement
                text = applyEmojis(text);

                const outMsg = {
                    type     : 'msg',
                    username : client.username,
                    text,
                    channel  : client.channel,
                    id,
                    ts       : Date.now(),
                };

                pushHistory(client.channel, outMsg);
                broadcast(client.channel, outMsg);

                // Mention detection — notify mentioned user specially
                const mentionRe = /@(\w{1,32})/g;
                let m;
                while ((m = mentionRe.exec(text)) !== null) {
                    const targetName = m[1];
                    const targetWs   = userIndex.get(targetName);
                    if (targetWs && targetWs !== ws && targetWs.readyState === WebSocket.OPEN) {
                        targetWs.send(JSON.stringify({
                            type    : 'mention',
                            from    : client.username,
                            text,
                            channel : client.channel,
                            ts      : Date.now(),
                        }));
                    }
                }
                break;
            }

            // ----------------------------------------------------------------
            // dm — direct message to a specific user
            // ----------------------------------------------------------------
            case 'dm': {
                if (!client.username) return;
                const toName = sanitize(msg.to, 32);
                const text   = sanitize(msg.text, 256);
                if (!toName || !text) return;

                const targetWs = userIndex.get(toName);
                if (!targetWs || targetWs.readyState !== WebSocket.OPEN) {
                    ws.send(JSON.stringify({ type: 'dm_error', reason: 'User not online', to: toName }));
                    return;
                }
                const dmPacket = {
                    type : 'dm',
                    from : client.username,
                    to   : toName,
                    text : applyEmojis(text),
                    ts   : Date.now(),
                };
                targetWs.send(JSON.stringify(dmPacket));
                // Echo back to sender as confirmation
                ws.send(JSON.stringify({ ...dmPacket, type: 'dm_sent' }));
                break;
            }

            // ----------------------------------------------------------------
            // status — set presence status and activity
            // ----------------------------------------------------------------
            case 'status': {
                if (!client.username) return;
                const validStatuses = ['online', 'away', 'dnd', 'offline'];
                const newStatus  = validStatuses.includes(msg.status) ? msg.status : 'online';
                const newActivity = sanitize(msg.activity || '', 128) || '';
                client.status   = newStatus;
                client.activity = newActivity;

                // Notify friends who are online
                for (const friendName of client.friends) {
                    const friendWs = userIndex.get(friendName);
                    if (friendWs && friendWs.readyState === WebSocket.OPEN) {
                        friendWs.send(JSON.stringify({
                            type    : 'friend_status',
                            username: client.username,
                            status  : newStatus,
                            activity: newActivity,
                            ts      : Date.now(),
                        }));
                    }
                }
                // Also notify any clients who have this user in their friend list
                for (const [otherWs, otherClient] of clients) {
                    if (otherWs !== ws && otherClient.friends.has(client.username) && otherWs.readyState === WebSocket.OPEN) {
                        otherWs.send(JSON.stringify({
                            type    : 'friend_status',
                            username: client.username,
                            status  : newStatus,
                            activity: newActivity,
                            ts      : Date.now(),
                        }));
                    }
                }
                ws.send(JSON.stringify({ type: 'status_ack', status: newStatus, activity: newActivity }));
                break;
            }

            // ----------------------------------------------------------------
            // friend_req — add a friend
            // ----------------------------------------------------------------
            case 'friend_req': {
                if (!client.username) return;
                const targetName = sanitize(msg.to, 32);
                if (!targetName || targetName === client.username) return;

                client.friends.add(targetName);

                // Send notification to the target if they are online
                const targetWs = userIndex.get(targetName);
                if (targetWs && targetWs.readyState === WebSocket.OPEN) {
                    targetWs.send(JSON.stringify({
                        type : 'friend_request',
                        from : client.username,
                        ts   : Date.now(),
                    }));
                }
                ws.send(JSON.stringify({ type: 'friend_added', username: targetName }));
                break;
            }

            // ----------------------------------------------------------------
            // friend_remove — remove a friend
            // ----------------------------------------------------------------
            case 'friend_remove': {
                if (!client.username) return;
                const targetName = sanitize(msg.username, 32);
                if (!targetName) return;
                client.friends.delete(targetName);
                ws.send(JSON.stringify({ type: 'friend_removed', username: targetName }));
                break;
            }

            // ----------------------------------------------------------------
            // friend_list — list friends and their statuses
            // ----------------------------------------------------------------
            case 'friend_list': {
                if (!client.username) return;
                const friendsList = [];
                for (const friendName of client.friends) {
                    const friendWs     = userIndex.get(friendName);
                    const friendClient = friendWs ? clients.get(friendWs) : null;
                    friendsList.push({
                        username: friendName,
                        online  : !!(friendWs && friendWs.readyState === WebSocket.OPEN),
                        status  : friendClient ? friendClient.status   : 'offline',
                        activity: friendClient ? friendClient.activity : '',
                    });
                }
                ws.send(JSON.stringify({ type: 'friend_list', friends: friendsList }));
                break;
            }

            // ----------------------------------------------------------------
            // userlist — list users in a channel
            // ----------------------------------------------------------------
            case 'userlist': {
                const targetChannel = sanitize(msg.channel || (client.channel || 'Global'), 32);
                const ch = channels.get(targetChannel);
                const users = [];
                if (ch) {
                    for (const memberWs of ch) {
                        const memberClient = clients.get(memberWs);
                        if (memberClient && memberClient.username) {
                            users.push({
                                username: memberClient.username,
                                status  : memberClient.status,
                                activity: memberClient.activity,
                            });
                        }
                    }
                }
                ws.send(JSON.stringify({ type: 'userlist', channel: targetChannel, users }));
                break;
            }

            // ----------------------------------------------------------------
            // channel_list — list all active channels
            // ----------------------------------------------------------------
            case 'channel_list': {
                const list = [];
                for (const [ch, members] of channels) {
                    list.push({ channel: ch, count: members.size });
                }
                ws.send(JSON.stringify({ type: 'channel_list', channels: list }));
                break;
            }

            // ----------------------------------------------------------------
            // reaction — react to a message (broadcast to channel)
            // ----------------------------------------------------------------
            case 'reaction': {
                if (!client.username || !client.channel) return;
                const emoji = sanitize(msg.emoji, 16);
                if (!emoji) return;
                broadcast(client.channel, {
                    type    : 'reaction',
                    from    : client.username,
                    emoji   : applyEmojis(emoji),
                    channel : client.channel,
                    ts      : Date.now(),
                });
                break;
            }

            // ----------------------------------------------------------------
            // party_join — join a named party channel
            // ----------------------------------------------------------------
            case 'party_join': {
                const partyChannel = sanitize(msg.channel, 64);
                if (!partyChannel || !client.username) return;

                if (client.channel) leave(ws, client);
                client.channel = 'party:' + partyChannel;
                if (!channels.has(client.channel)) channels.set(client.channel, new Set());
                channels.get(client.channel).add(ws);

                ws.send(JSON.stringify({
                    type    : 'party_joined',
                    channel : client.channel,
                    members : channels.get(client.channel).size,
                }));
                break;
            }

            // ----------------------------------------------------------------
            // party_update — broadcast health/coords to party
            // ----------------------------------------------------------------
            case 'party_update': {
                if (!client.username || !client.channel || !client.channel.startsWith('party:')) return;
                broadcast(client.channel, {
                    type    : 'party_update',
                    username: client.username,
                    health  : typeof msg.health === 'number' ? Math.max(0, Math.min(20, msg.health)) : null,
                    coords  : sanitize(msg.coords || '', 64) || null,
                    ts      : Date.now(),
                }, ws);
                break;
            }

            // ----------------------------------------------------------------
            // ping
            // ----------------------------------------------------------------
            case 'ping':
                ws.send(JSON.stringify({ type: 'pong', ts: Date.now() }));
                break;

            default:
                // Unknown message types are silently ignored
                break;
        }
    });

    ws.on('close', () => {
        const client = clients.get(ws);
        if (client) {
            if (client.username) userIndex.delete(client.username);
            leave(ws, client);
        }
        clients.delete(ws);
    });

    ws.on('error', () => ws.terminate());
});

// ---------------------------------------------------------------------------
// Helper: leave a channel
// ---------------------------------------------------------------------------
function leave(ws, client) {
    if (!client.channel) return;
    const ch = channels.get(client.channel);
    if (ch) {
        ch.delete(ws);
        if (ch.size === 0) {
            channels.delete(client.channel);
        } else {
            broadcast(client.channel, {
                type     : 'system',
                text     : `§c${client.username} §7left §b${client.channel}`,
                count    : ch.size,
                username : client.username,
                event    : 'leave',
            }, ws);
        }
    }
    client.channel = null;
}

// ---------------------------------------------------------------------------
// Helper: broadcast to a channel
// ---------------------------------------------------------------------------
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

// ---------------------------------------------------------------------------
// Helper: sanitize user input
// ---------------------------------------------------------------------------
function sanitize(str, maxLen) {
    if (typeof str !== 'string') return null;
    return str.replace(/[^\w\s\-.!?#@:'"()À-ɏ😀-🙏🌀-🗿]/gu, '').trim().slice(0, maxLen) || null;
}

// ---------------------------------------------------------------------------
// Start
// ---------------------------------------------------------------------------
server.listen(PORT, () => {
    console.log(`[Quark Chat Server] Listening on ws://0.0.0.0:${PORT}`);
    console.log(`[Quark Chat Server] Health check: http://localhost:${PORT}/health`);
});

// Graceful shutdown
process.on('SIGTERM', () => { wss.close(); server.close(); });
process.on('SIGINT',  () => { wss.close(); server.close(); process.exit(0); });
