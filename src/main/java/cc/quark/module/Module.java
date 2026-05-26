package cc.quark.module;

import cc.quark.setting.*;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for every Quark module (hack).
 *
 * <p>Subclasses register their settings in the constructor via {@link #register}.
 * Event handling is done through {@link cc.quark.event.EventHandler}-annotated
 * public methods which are picked up by the EventBus when the module is registered.
 *
 * <p>Override {@link #onEnable()}, {@link #onDisable()}, and {@link #onTick()} as needed.
 */
public abstract class Module {

    /** Shared MC instance â€” safe to access from module code on the render/game thread. */
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String   name;
    private final String   description;
    private final Category category;

    private int     keybind;
    private boolean enabled;
    private boolean visible;   // whether shown in the HUD array list

    private final List<Setting<?>> settings = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    protected Module(String name, String description, Category category, int keybind) {
        this.name        = name;
        this.description = description;
        this.category    = category;
        this.keybind     = keybind;
        this.enabled     = false;
        this.visible     = true;
    }

    protected Module(String name, String description, Category category) {
        this(name, description, category, -1);
    }

    // -------------------------------------------------------------------------
    // Lifecycle hooks â€” override in subclasses
    // -------------------------------------------------------------------------

    /** Called once when the module is turned on. */
    public void onEnable() {}

    /** Called once when the module is turned off. */
    public void onDisable() {}

    /**
     * Called every client tick while the module is enabled.
     * Lightweight logic (not rendering) goes here.
     */
    public void onTick() {}

    // -------------------------------------------------------------------------
    // Enable / disable / toggle
    // -------------------------------------------------------------------------

    public final void toggle() {
        if (enabled) disable(); else enable();
    }

    public final void enable() {
        if (enabled) return;
        enabled = true;
        cc.quark.Quark.getInstance().getEventBus().subscribe(this);
        onEnable();
    }

    public final void disable() {
        if (!enabled) return;
        enabled = false;
        cc.quark.Quark.getInstance().getEventBus().unsubscribe(this);
        onDisable();
    }

    // -------------------------------------------------------------------------
    // Settings registration and lookup
    // -------------------------------------------------------------------------

    /**
     * Register a setting so it is tracked for GUI display and config persistence.
     *
     * @param setting the setting to register
     * @param <T>     concrete setting type
     * @return the same setting (for chained field assignment)
     */
    protected <T extends Setting<?>> T register(T setting) {
        settings.add(setting);
        return setting;
    }

    /** Alias for {@link #register} to match the addSetting() naming convention. */
    protected <T extends Setting<?>> T addSetting(T setting) {
        return register(setting);
    }

    /**
     * Retrieve a setting's current value by name.
     *
     * @param name the setting name (case-insensitive)
     * @return the setting value cast to T, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getSetting(String name) {
        for (Setting<?> s : settings) {
            if (s.getName().equalsIgnoreCase(name)) {
                return (T) s.getValue();
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Convenience setting factories
    // -------------------------------------------------------------------------

    protected BoolSetting boolSetting(String name, String description, boolean defaultValue) {
        return register(new BoolSetting(name, description, defaultValue));
    }

    protected DoubleSetting doubleSetting(String name, String description,
                                          double defaultValue, double min, double max) {
        return register(new DoubleSetting(name, description, defaultValue, min, max));
    }

    protected IntSetting intSetting(String name, String description,
                                    int defaultValue, int min, int max) {
        return register(new IntSetting(name, description, defaultValue, min, max));
    }

    protected ModeSetting modeSetting(String name, String description,
                                      String defaultMode, String... modes) {
        return register(new ModeSetting(name, description, defaultMode, modes));
    }

    protected ColorSetting colorSetting(String name, String description, int defaultArgb) {
        return register(new ColorSetting(name, description, defaultArgb));
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public String getName()        { return name; }
    public String getDescription() { return description; }
    public Category getCategory()  { return category; }

    public int  getKeybind()               { return keybind; }
    public void setKeybind(int keybind)    { this.keybind = keybind; }

    public boolean isEnabled()             { return enabled; }

    public boolean isVisible()             { return visible; }
    public void    setVisible(boolean vis) { this.visible = vis; }

    public List<Setting<?>> getSettings()  { return settings; }

    @Override
    public String toString() {
        return name + " [" + category + "] " + (enabled ? "ON" : "OFF");
    }
}
