# Quark — Brand Site

The public landing page for the Quark client. A static, single-page marketing
site: hero, feature grid, the real module catalog, a "how it works" walkthrough,
download CTAs and an FAQ. No backend, no telemetry, no build step.

```
website/
├── index.html   The page
├── style.css    Brand theme (purple/cyan)
└── app.js       Renders the module catalog + nav scroll state
```

## Local preview

```bash
cd website
python3 -m http.server 8123    # then open http://localhost:8123
```

## Deploy (Vercel)

This folder is the Vercel project's **Root Directory** (`website`). It's a plain
static site — Vercel serves `index.html` directly, no framework or build command
needed. Pushing to the connected branch redeploys automatically.

The module list in `app.js` mirrors `launcher/agent/StandaloneClient.java`; keep
the two in sync when modules change.
