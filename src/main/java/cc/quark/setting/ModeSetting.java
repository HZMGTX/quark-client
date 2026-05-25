package cc.quark.setting;

import java.util.Arrays;
import java.util.List;

/**
 * A String-based mode setting backed by a fixed list of valid options.
 * Useful for things like "Mode: Vanilla / AAC / Hypixel".
 */
public class ModeSetting extends Setting<String> {

    private final List<String> modes;

    public ModeSetting(String name, String description, String defaultMode, String... modes) {
        super(name, description, defaultMode);
        this.modes = Arrays.asList(modes);
        if (!this.modes.contains(defaultMode)) {
            throw new IllegalArgumentException(
                    "Default mode '" + defaultMode + "' is not in the modes list.");
        }
    }

    @Override
    public void setValue(String value) {
        // Only accept values that are in the modes list (case-insensitive).
        for (String mode : modes) {
            if (mode.equalsIgnoreCase(value)) {
                this.value = mode;
                return;
            }
        }
        // Silently ignore invalid values to avoid crashes from bad config.
    }

    /**
     * Cycle to the next mode in the list, wrapping around.
     */
    public void cycle() {
        int idx = modes.indexOf(value);
        this.value = modes.get((idx + 1) % modes.size());
    }

    public void next() { cycle(); }

    public void previous() {
        int idx = modes.indexOf(value);
        this.value = modes.get((idx - 1 + modes.size()) % modes.size());
    }

    public List<String> getModes() {
        return modes;
    }

    /**
     * Returns true when the current mode equals {@code mode} (case-insensitive).
     */
    public boolean is(String mode) {
        return value.equalsIgnoreCase(mode);
    }

    /** Convenience getter. */
    public String get() {
        return value;
    }
}
