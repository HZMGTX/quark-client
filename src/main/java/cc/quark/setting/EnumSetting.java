package cc.quark.setting;

/**
 * An enum-based mode setting.
 *
 * @param <E> the enum type
 */
public class EnumSetting<E extends Enum<E>> extends Setting<E> {

    private final E[] values;

    @SuppressWarnings("unchecked")
    public EnumSetting(String name, String description, E defaultValue) {
        super(name, description, defaultValue);
        this.values = (E[]) defaultValue.getClass().getEnumConstants();
    }

    /** Cycles to the next enum value. */
    public void cycle() {
        int next = (value.ordinal() + 1) % values.length;
        value = values[next];
    }

    public E[] getValues() {
        return values;
    }

    /** Convenience getter. */
    public E get() {
        return value;
    }
}
