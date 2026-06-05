package cc.quark.compat;

import net.minecraft.SharedConstants;

/**
 * Utilities for version-specific behavior at runtime.
 * Use these helpers when code paths differ across Minecraft versions.
 */
public final class MCVersion {

    private MCVersion() {}

    /** Returns the current Minecraft version string, e.g. "1.21.1". */
    public static String get() {
        return SharedConstants.getGameVersion().getName();
    }

    /** Returns the network protocol version number for the running game. */
    public static int getProtocol() {
        return SharedConstants.getGameVersion().getProtocolVersion();
    }

    /**
     * Returns true if the running version is lexicographically >= the given string.
     * Works correctly for versions in the form "1.X.Y".
     */
    public static boolean isAtLeast(String version) {
        return get().compareTo(version) >= 0;
    }

    public static boolean is1_21OrHigher() {
        return get().compareTo("1.21") >= 0;
    }

    public static boolean is1_20OrHigher() {
        return get().compareTo("1.20") >= 0;
    }

    public static boolean is1_19OrHigher() {
        return get().compareTo("1.19") >= 0;
    }

    public static boolean is1_18OrHigher() {
        return get().compareTo("1.18") >= 0;
    }

    public static boolean is1_21() {
        return get().startsWith("1.21");
    }

    public static boolean is1_20() {
        return get().startsWith("1.20");
    }

    public static boolean is1_19() {
        return get().startsWith("1.19");
    }

    public static boolean is1_18() {
        return get().startsWith("1.18");
    }
}
