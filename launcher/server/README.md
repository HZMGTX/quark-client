# Quark Global Chat — Relay Server

This is the real backend behind the launcher's **Global Chat** page. It is a
small WebSocket relay that:

- keeps an accurate **online count** (presence),
- **broadcasts** every message to all connected clients,
- replays the last 50 messages as **history** to anyone who joins,
- applies **rate-limiting** (8 msgs / 10s per connection), a 240-char cap and
  control-character stripping,
- exposes a `GET /health` endpoint for uptime probes,
- drops dead connections with a 30s ping/pong **heartbeat**.

No database is needed — recent history is held in memory.

## Run locally

```bash
cd launcher/server
npm install
PORT=8787 npm start
```

Then in the launcher: **Settings → Global Chat → Relay URL** → `ws://localhost:8787`

## Deploy

Any host that can hold a long-lived WebSocket works (a VPS, Fly.io, Railway,
Render, Koyeb, a Docker box, …). Two things to get right:

1. **Bind the platform port.** The server reads `PORT` from the environment,
   which is what most platforms inject. Locally it defaults to `8787`.
2. **Use `wss://` in production.** Put the server behind TLS (most platforms
   terminate HTTPS for you) and point the launcher at `wss://your-host`.

Example (Fly.io / Railway / Render): set the start command to `node chat-relay.js`,
expose the web port, and copy the public URL into the launcher with the `wss://`
scheme.

## Protocol

JSON text frames:

| Direction | Message |
|-----------|---------|
| client → server | `{ "type": "join", "user": "Name" }` |
| client → server | `{ "type": "chat", "text": "hello" }` |
| server → client | `{ "type": "welcome", "online": N, "you": "Name" }` |
| server → client | `{ "type": "history", "messages": [ {user,text,ts} ] }` |
| server → client | `{ "type": "presence", "online": N }` |
| server → client | `{ "type": "msg", "user", "text", "ts" }` |
| server → client | `{ "type": "system", "text", "ts" }` |
| server → client | `{ "type": "error", "text" }` |

The launcher connects with the browser `WebSocket` API and reconnects with
backoff if the connection drops. If no Relay URL is configured, Global Chat
shows an honest "not configured" state instead of pretending to be online.
