package cc.quark.setting;

/**
 * Base class for all module settings.
 *
 * @param <T> the type of value this setting holds
 */
public abstract class Setting<T> {

    private final String name;
    private final String description;
    protected T value;

    public Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return name + "=" + value;
    }
}
