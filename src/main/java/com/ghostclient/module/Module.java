package com.ghostclient.module;

import com.ghostclient.setting.Setting;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all GhostClient modules (hacks).
 * Subclasses register their settings in the constructor and implement
 * logic via {@link com.ghostclient.event.EventHandler}-annotated methods.
 */
public abstract class Module {

    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final String description;
    private final Category category;

    private int keybind;
    private boolean enabled;
    private boolean visible;

    private final List<Setting<?>> settings = new ArrayList<>();

    protected Module(String name, String description, Category category, int keybind) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.keybind = keybind;
        this.enabled = false;
        this.visible = true;
    }

    protected Module(String name, String description, Category category) {
        this(name, description, category, -1);
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /** Called when the module is enabled. */
    public void onEnable() {}

    /** Called when the module is disabled. */
    public void onDisable() {}

    /** Toggle this module on/off. */
    public final void toggle() {
        if (enabled) {
            disable();
        } else {
            enable();
        }
    }

    public final void enable() {
        enabled = true;
        onEnable();
    }

    public final void disable() {
        enabled = false;
        onDisable();
    }

    // -------------------------------------------------------------------------
    // Settings registration
    // -------------------------------------------------------------------------

    /** Register a setting so it appears in the GUI / config. */
    protected <T extends Setting<?>> T register(T setting) {
        settings.add(setting);
        return setting;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public int getKeybind() {
        return keybind;
    }

    public void setKeybind(int keybind) {
        this.keybind = keybind;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    @Override
    public String toString() {
        return name + " [" + category + "]";
    }
}
