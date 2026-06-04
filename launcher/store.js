'use strict';
const { app } = require('electron');
const path = require('path');
const fs   = require('fs');

class Store {
    constructor(name = 'config') {
        this._path = path.join(app.getPath('userData'), `quark-${name}.json`);
        this._data = {};
        try {
            this._data = JSON.parse(fs.readFileSync(this._path, 'utf8'));
        } catch (_) { /* first run */ }
    }

    get(key, defaultValue) {
        return Object.prototype.hasOwnProperty.call(this._data, key)
            ? this._data[key]
            : defaultValue;
    }

    set(key, value) {
        this._data[key] = value;
        this._save();
    }

    delete(key) {
        delete this._data[key];
        this._save();
    }

    clear() {
        this._data = {};
        this._save();
    }

    get store() { return { ...this._data }; }

    _save() {
        try { fs.writeFileSync(this._path, JSON.stringify(this._data, null, 2)); }
        catch (e) { console.error('[Store] write error:', e); }
    }
}

module.exports = Store;
