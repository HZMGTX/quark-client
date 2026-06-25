'use strict';

// The real injected-client module catalog — mirrors launcher/agent/StandaloneClient.java.
const MODULES = {
  Render: ['FullBright', 'Zoom'],
  HUD: ['Watermark', 'ModuleList', 'FPS', 'Keystrokes', 'CPS', 'Coordinates',
        'ArmorStatus', 'Ping', 'Direction', 'Clock', 'Health', 'Hunger',
        'Speed', 'HeldItem', 'ServerIP', 'GameTime', 'Memory', 'SessionInfo'],
  Misc: ['ClickGui', 'ConfigManager', 'Notifications'],
};

function renderModules() {
  const host = document.getElementById('module-cols');
  if (!host) return;
  host.innerHTML = Object.entries(MODULES).map(([cat, mods]) => `
    <div class="module-col">
      <h3>${cat} · ${mods.length}</h3>
      <div class="chips">
        ${mods.map(m => `<span class="chip">${m}</span>`).join('')}
      </div>
    </div>`).join('');
}

function onScroll() {
  const nav = document.getElementById('nav');
  if (nav) nav.classList.toggle('scrolled', window.scrollY > 8);
}

renderModules();
onScroll();
window.addEventListener('scroll', onScroll, { passive: true });
