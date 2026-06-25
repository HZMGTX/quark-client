'use strict';

// The real injected-client module catalog — mirrors launcher/agent/StandaloneClient.java.
// Boolean values reflect each module's actual default-enabled state from buildModules().
const MODULES = {
  Render: { FullBright: false, Zoom: false },
  HUD: {
    Watermark: true, ModuleList: true, FPS: false, FpsGraph: false, Keystrokes: true,
    CPS: false, Coordinates: true, ArmorStatus: false, Ping: false, Direction: false,
    Clock: false, Health: false, Hunger: false, Speed: false, HeldItem: false,
    ServerIP: false, GameTime: false, Memory: false, SessionInfo: false,
  },
  Misc: { ClickGui: true, ConfigManager: true, Notifications: true },
};

function renderModules() {
  const host = document.getElementById('module-cols');
  if (!host) return;
  host.innerHTML = Object.entries(MODULES).map(([cat, mods]) => {
    const names = Object.keys(mods);
    const rows = names.map(name => `
      <div class="module-row">
        <span>${name}</span>
        <span class="toggle ${mods[name] ? 'on' : ''}" title="${mods[name] ? 'On by default' : 'Off by default'}"></span>
      </div>`).join('');
    return `
      <div class="module-col">
        <h3>${cat}<span>${names.length}</span></h3>
        ${rows}
      </div>`;
  }).join('');
}

function onScroll() {
  const nav = document.getElementById('nav');
  if (nav) nav.classList.toggle('scrolled', window.scrollY > 8);
}

renderModules();
onScroll();
window.addEventListener('scroll', onScroll, { passive: true });
