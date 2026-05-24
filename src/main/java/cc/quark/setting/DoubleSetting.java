package cc.quark.setting;

/**
 * A numeric double setting with min/max bounds.
 */
public class DoubleSetting extends Setting<Double> {

    private final double min;
    private final double max;

    public DoubleSetting(String name, String description, double defaultValue, double min, double max) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
    }

    @Override
    public void setValue(Double value) {
        this.value = Math.max(min, Math.min(max, value));
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    /** Convenience getter returning a primitive double. */
    public double get() {
        return value;
    }
}
