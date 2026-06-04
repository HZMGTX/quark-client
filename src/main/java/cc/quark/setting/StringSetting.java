package cc.quark.setting;

/**
 * A free-text string setting.
 */
public class StringSetting extends Setting<String> {

    public StringSetting(String name, String description, String defaultValue) {
        super(name, description, defaultValue);
    }

    /** Convenience getter. */
    public String get() {
        return value;
    }
}
