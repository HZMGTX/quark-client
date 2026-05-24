package com.ghostclient.setting;

/**
 * A numeric integer setting with min/max bounds.
 */
public class IntSetting extends Setting<Integer> {

    private final int min;
    private final int max;

    public IntSetting(String name, String description, int defaultValue, int min, int max) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
    }

    @Override
    public void setValue(Integer value) {
        this.value = Math.max(min, Math.min(max, value));
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    /** Convenience getter returning a primitive int. */
    public int get() {
        return value;
    }
}
