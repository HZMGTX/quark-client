package cc.quark.setting;

/**
 * A color setting stored as a packed ARGB integer.
 *
 * <p>Helpers are provided for extracting individual ARGB channels and for
 * constructing the packed int from separate channel values.
 *
 * <p>Example default: {@code 0xFF00AAFF} = fully-opaque cyan.
 */
public class ColorSetting extends Setting<Integer> {

    public ColorSetting(String name, String description, int defaultArgb) {
        super(name, description, defaultArgb);
    }

    // -------------------------------------------------------------------------
    // Convenience channel extractors
    // -------------------------------------------------------------------------

    public int getAlpha() {
        return (value >> 24) & 0xFF;
    }

    public int getRed() {
        return (value >> 16) & 0xFF;
    }

    public int getGreen() {
        return (value >> 8) & 0xFF;
    }

    public int getBlue() {
        return value & 0xFF;
    }

    // -------------------------------------------------------------------------
    // Channel setters (rebuild the packed int)
    // -------------------------------------------------------------------------

    public void setAlpha(int alpha) {
        value = pack(alpha, getRed(), getGreen(), getBlue());
    }

    public void setRed(int red) {
        value = pack(getAlpha(), red, getGreen(), getBlue());
    }

    public void setGreen(int green) {
        value = pack(getAlpha(), getRed(), green, getBlue());
    }

    public void setBlue(int blue) {
        value = pack(getAlpha(), getRed(), getGreen(), blue);
    }

    // -------------------------------------------------------------------------
    // Floating-point accessors (0.0 â€“ 1.0 range)
    // -------------------------------------------------------------------------

    public float getAlphaF() { return getAlpha() / 255f; }
    public float getRedF()   { return getRed()   / 255f; }
    public float getGreenF() { return getGreen() / 255f; }
    public float getBlueF()  { return getBlue()  / 255f; }

    // -------------------------------------------------------------------------
    // Factory helpers
    // -------------------------------------------------------------------------

    /** Build a packed ARGB int from four channel values (0â€“255 each). */
    public static int pack(int alpha, int red, int green, int blue) {
        return ((alpha & 0xFF) << 24)
             | ((red   & 0xFF) << 16)
             | ((green & 0xFF) << 8)
             | (blue   & 0xFF);
    }

    /** Convenience getter returning the raw ARGB int. */
    public int get() {
        return value;
    }
}
