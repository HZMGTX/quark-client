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
    injectScan      : ()          => ipcRenderer.invoke('inject:scan'),
    injectRun       : (pid)       => ipcRenderer.invoke('inject:run', pid),
    injectAutoStart : ()          => ipcRenderer.invoke('inject:autoStart'),
    injectAutoStop  : ()          => ipcRenderer.invoke('inject:autoStop'),

    // Persistent settings
    settingsGet    : (key)        => ipcRenderer.invoke('settings:get', key),
    settingsSet    : (key, val)   => ipcRenderer.invoke('settings:set', key, val),
    settingsGetAll : ()           => ipcRenderer.invoke('settings:getAll'),

    // System info
    systemInfo      : ()          => ipcRenderer.invoke('system:info'),
    systemGameDirs  : ()          => ipcRenderer.invoke('system:gameDirs'),
    openFolder      : (p)         => ipcRenderer.invoke('system:openFolder', p),
    selectFile      : ()          => ipcRenderer.invoke('system:selectFile'),
    openExternal    : (url)       => ipcRenderer.invoke('system:openExternal', url),

    // Java discovery
    javaList        : ()          => ipcRenderer.invoke('java:list'),

    // Server ping
    serverPing      : (host, port)=> ipcRenderer.invoke('server:ping', host, port),

    // Config backup
    configExport    : ()          => ipcRenderer.invoke('config:export'),
    configImport    : ()          => ipcRenderer.invoke('config:import'),

    // Push events from main → renderer
    onInjectLog      : (cb)  => ipcRenderer.on('inject:log',         (_e, d)    => cb(d)),
    onAutoDetected   : (cb)  => ipcRenderer.on('inject:autoDetected',(_e, proc) => cb(proc)),
    onNavigate       : (cb)  => ipcRenderer.on('navigate',           (_e, page) => cb(page)),
});
