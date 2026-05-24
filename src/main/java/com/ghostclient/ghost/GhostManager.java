package com.ghostclient.ghost;

/**
 * GhostManager - central anti-detection manager for GhostClient.
 *
 * <p>Stores the active {@link AntiCheatProfile} and exposes safe limits for
 * speed, reach, attack timing, rotation behaviour, and velocity modification
 * that modules should respect to avoid flagging that profile's anti-cheat.
 *
 * <p>Usage:
 * <pre>
 *   GhostManager.INSTANCE.setProfile(AntiCheatProfile.GRIM);
 *   double safeSpeed = GhostManager.INSTANCE.getMaxSpeed();
 * </pre>
 */
public class GhostManager {

    /** Singleton instance. */
    public static final GhostManager INSTANCE = new GhostManager();

    private AntiCheatProfile activeProfile = AntiCheatProfile.GRIM;

    private final HumanizationEngine humanizer  = new HumanizationEngine();
    private final PacketShaper       packetShaper = new PacketShaper();

    // -------------------------------------------------------------------------
    // Profile enum
    // -------------------------------------------------------------------------

    public enum AntiCheatProfile {
        /** No anti-cheat present — full module power. */
        VANILLA,
        /** NoCheatPlus — older, widely deployed. */
        NCP,
        /** Advanced Anti Cheat. */
        AAC,
        /** GrimAC — modern, prediction-based, very strict. */
        GRIM,
        /** Hypixel Watchdog — fast and lenient on speed, strict on reach/combat. */
        WATCHDOG,
        /** Spartan Anti Cheat. */
        SPARTAN,
        /** Matrix Anti Cheat. */
        MATRIX,
        /** Intave AC. */
        INTAVE,
        /** Manual tuning — no limits applied by default. */
        CUSTOM
    }

    // -------------------------------------------------------------------------
    // Profile access
    // -------------------------------------------------------------------------

    public AntiCheatProfile getActiveProfile() {
        return activeProfile;
    }

    public void setProfile(AntiCheatProfile profile) {
        this.activeProfile = profile;
    }

    public void setActiveProfile(AntiCheatProfile profile) {
        this.activeProfile = profile;
    }

    // -------------------------------------------------------------------------
    // Safe movement limits
    // -------------------------------------------------------------------------

    /**
     * Returns the maximum horizontal movement speed (blocks/tick) considered safe
     * for the current anti-cheat profile.
     */
    public double getMaxSpeed() {
        return switch (activeProfile) {
            case VANILLA  -> 1.0;
            case NCP      -> AntiCheatBypass.NCP.getSafeSpeed(true);       // 0.287
            case AAC      -> AntiCheatBypass.AAC.getSafeSpeed();            // 0.26
            case GRIM     -> AntiCheatBypass.Grim.getSafeSpeed();           // 0.2806
            case WATCHDOG -> AntiCheatBypass.Watchdog.getSafeSpeed();       // 0.35
            case SPARTAN  -> AntiCheatBypass.Spartan.getSafeSpeed();        // 0.36
            case MATRIX   -> AntiCheatBypass.Matrix.getSafeSpeed();         // 0.34
            case INTAVE   -> 0.29;
            case CUSTOM   -> 0.35;
        };
    }

    // -------------------------------------------------------------------------
    // Safe reach limits
    // -------------------------------------------------------------------------

    /**
     * Returns the maximum attack reach (blocks) considered safe for the current profile.
     */
    public double getMaxReach() {
        return switch (activeProfile) {
            case VANILLA  -> 6.0;
            case NCP      -> 3.1;
            case AAC      -> 3.1;
            case GRIM     -> AntiCheatBypass.Grim.getMaxReach();            // 3.0
            case WATCHDOG -> AntiCheatBypass.Watchdog.getSafeReach();       // 3.2
            case SPARTAN  -> 3.2;
            case MATRIX   -> AntiCheatBypass.Matrix.getSafeReach();         // 3.15
            case INTAVE   -> 3.0;
            case CUSTOM   -> 3.5;
        };
    }

    // -------------------------------------------------------------------------
    // Attack timing
    // -------------------------------------------------------------------------

    /**
     * Returns the minimum milliseconds that must pass between consecutive attacks
     * to avoid combat-speed flags.
     */
    public long getMinAttackDelay() {
        return switch (activeProfile) {
            case VANILLA  -> 0L;
            case NCP      -> 50L;
            case AAC      -> 60L;
            case GRIM     -> 75L;
            case WATCHDOG -> (long) (AntiCheatBypass.Watchdog.getAttackCooldown() * 50L); // 300 ms
            case SPARTAN  -> 55L;
            case MATRIX   -> 65L;
            case INTAVE   -> 70L;
            case CUSTOM   -> 60L;
        };
    }

    // -------------------------------------------------------------------------
    // Rotation behaviour
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} when the current profile requires rotations to look
     * visually legitimate (smooth, not instant snapping).
     */
    public boolean shouldRotateLegit() {
        return switch (activeProfile) {
            case VANILLA, CUSTOM -> false;
            default              -> true;
        };
    }

    /**
     * Returns the maximum degrees per tick the player's view can rotate without
     * triggering aim-speed checks for the current profile.
     */
    public double getRotationSpeed() {
        return switch (activeProfile) {
            case VANILLA  -> 180.0;
            case NCP      -> 30.0;
            case AAC      -> 25.0;
            case GRIM     -> 20.0;
            case WATCHDOG -> 35.0;
            case SPARTAN  -> 35.0;
            case MATRIX   -> 30.0;
            case INTAVE   -> 25.0;
            case CUSTOM   -> 40.0;
        };
    }

    // -------------------------------------------------------------------------
    // Strafe / movement behaviour
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} when strafe-attacking (moving sideways while attacking)
     * is considered safe for the current profile.
     */
    public boolean canStrafeAttack() {
        return switch (activeProfile) {
            case GRIM, INTAVE -> false;
            default           -> true;
        };
    }

    // -------------------------------------------------------------------------
    // Velocity modification
    // -------------------------------------------------------------------------

    /**
     * Returns a multiplier (0.0 – 1.0) for incoming knockback velocity that
     * modules may apply without triggering velocity-reduction flags.
     *
     * <p>1.0 = full vanilla knockback (safe); 0.0 = no knockback (likely flagged).
     */
    public double getVelocityMultiplier() {
        return switch (activeProfile) {
            case VANILLA  -> 1.0;
            case NCP      -> AntiCheatBypass.NCP.useAcceleration() ? 0.6 : 0.8;
            case AAC      -> AntiCheatBypass.AAC.getAirControl(); // 0.98
            case GRIM     -> 1.0;  // Grim simulates vanilla; any reduction flags instantly
            case WATCHDOG -> 0.7;
            case SPARTAN  -> 0.65;
            case MATRIX   -> 0.75;
            case INTAVE   -> 0.8;
            case CUSTOM   -> 0.7;
        };
    }

    // -------------------------------------------------------------------------
    // Sub-system accessors
    // -------------------------------------------------------------------------

    public HumanizationEngine getHumanizer() {
        return humanizer;
    }

    public PacketShaper getPacketShaper() {
        return packetShaper;
    }
}
