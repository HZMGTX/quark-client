'use strict';
const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('quark', {
    // Window controls
    minimize : ()        => ipcRenderer.invoke('window:minimize'),
    close    : ()        => ipcRenderer.invoke('window:close'),

    // Discord OAuth
    discordLogin  : ()   => ipcRenderer.invoke('discord:login'),
    discordLogout : ()   => ipcRenderer.invoke('discord:logout'),

    // Injection
    injectScan : ()      => ipcRenderer.invoke('inject:scan'),
    injectRun  : (pid)   => ipcRenderer.invoke('inject:run', pid),

    // Persistent settings
    settingsGet    : (key)       => ipcRenderer.invoke('settings:get', key),
    settingsSet    : (key, val)  => ipcRenderer.invoke('settings:set', key, val),
    settingsGetAll : ()          => ipcRenderer.invoke('settings:getAll'),

    // App info
    getVersion : () => ipcRenderer.invoke('app:version'),

    // Global Chat Server
    chatServerStart  : (port) => ipcRenderer.invoke('chat:serverStart', port),
    chatServerStop   : ()     => ipcRenderer.invoke('chat:serverStop'),
    chatServerStatus : ()     => ipcRenderer.invoke('chat:serverStatus'),
});
