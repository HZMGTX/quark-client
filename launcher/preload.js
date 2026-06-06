'use strict';
const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('quark', {
    // Window controls
    minimize : ()           => ipcRenderer.invoke('window:minimize'),
    close    : ()           => ipcRenderer.invoke('window:close'),
    maximize : ()           => ipcRenderer.invoke('window:maximize'),

    // App info
    version  : ()           => ipcRenderer.invoke('app:version'),

    // Discord OAuth
    discordLogin  : ()      => ipcRenderer.invoke('discord:login'),
    discordLogout : ()      => ipcRenderer.invoke('discord:logout'),

    // Injection
    injectScan : ()         => ipcRenderer.invoke('inject:scan'),
    injectRun  : (pid)      => ipcRenderer.invoke('inject:run', pid),

    // Persistent settings
    settingsGet    : (key)      => ipcRenderer.invoke('settings:get', key),
    settingsSet    : (key, val) => ipcRenderer.invoke('settings:set', key, val),
    settingsGetAll : ()         => ipcRenderer.invoke('settings:getAll'),

    // System info
    systemInfo       : ()       => ipcRenderer.invoke('system:info'),
    openFolder       : (p)      => ipcRenderer.invoke('system:openFolder', p),
    selectFile       : ()       => ipcRenderer.invoke('system:selectFile'),

    // Global Chat Server
    chatServerStart  : (port)   => ipcRenderer.invoke('chat:serverStart', port),
    chatServerStop   : ()       => ipcRenderer.invoke('chat:serverStop'),
    chatServerStatus : ()       => ipcRenderer.invoke('chat:serverStatus'),

    // Push event from main to renderer (for inject progress updates)
    onInjectLog : (cb) => ipcRenderer.on('inject:log', (_e, msg) => cb(msg)),
});
