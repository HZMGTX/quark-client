# Quark Stats — Live Tracking Dashboard

A small website that shows real, live activity from the Quark launcher and
the in-game client: how many installs are online, what they're doing, and
which modules actually get used. There is no fake data anywhere — every
number on the page is produced by a real `POST /api/event` call made by the
launcher or the client agent.

```
website/
├── server/   Telemetry backend (Node core only, no npm dependencies)
└── public/   The dashboard itself (static HTML/CSS/JS)
```

## Run locally

```bash
cd website/server
PORT=8788 npm start
```

Open `http://localhost:8788` — the same server answers the API and serves
the dashboard.

Then point the launcher at it: **Settings → Stats & Analytics → Stats Server
URL** → `http://localhost:8788`. Leave it blank and nothing is ever sent —
telemetry is opt-in, same as Global Chat.

## What it tracks

| Event type      | Source            | Meaning                                   |
|------------------|--------------------|--------------------------------------------|
| `launcher_start` | launcher           | The launcher app was opened                |
| `inject_success` | launcher           | An attach to a Minecraft process succeeded |
| `inject_failure` | launcher           | An attach attempt failed                   |
| `alt_switch`     | launcher           | The active account was changed             |
| `chat_connect`   | launcher           | Global Chat connected to a relay           |
| `config_export`  | launcher           | Settings were exported to a file           |
| `config_import`  | launcher           | Settings were imported from a file         |
| `discord_login`  | launcher           | Discord account linked                     |
| `discord_logout` | launcher           | Discord account unlinked                   |
| `server_ping`    | launcher           | A server was pinged (`{ online }` only — never the host/port) |
| `session_start`  | client             | The Java agent finished loading in-game    |
| `module_toggle`  | client             | A HUD/render module was turned on/off      |
| `menu_open`      | client             | The ClickGUI was opened                    |

No usernames, tokens, or IPs are ever included in a payload — only an
anonymous, randomly generated client id (so the dashboard can count unique
installs) and the event's own small JSON payload (e.g. `{ module: "Zoom",
enabled: true }`).

## Pages

| Page             | Shows                                                          |
|------------------|------------------------------------------------------------------|
| `/`              | Live dashboard — online count, totals, activity chart, recent feed |
| `/modules.html`  | The real module catalog grouped by category, with live use counts and share of all toggles |

## API

| Method | Path           | Body / Query                                              |
|--------|----------------|-------------------------------------------------------------|
| GET    | `/health`      | —                                                             |
| GET    | `/api/stats`   | — returns the aggregate snapshot the dashboard renders       |
| GET    | `/api/modules` | — returns the module catalog merged with live `uses`/`share` counts |
| POST   | `/api/event`   | `{ source: "launcher"\|"client", type, clientId, payload? }` |

Events are rate-limited per IP (120/min) and payloads are capped at 2 KB.
Aggregate counters are flushed to `server/data/stats.json` every 10 seconds
so a restart doesn't lose history; that file is git-ignored.

## Deploy

Same story as the chat relay: any host that can run a long-lived Node
process works (a VPS, Fly.io, Railway, Render, a Docker box, …). Bind the
platform's `PORT` env var and put it behind TLS so the launcher can use
`https://` for the Stats Server URL.
