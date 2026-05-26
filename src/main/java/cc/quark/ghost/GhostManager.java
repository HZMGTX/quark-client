package cc.quark.ghost;

/**
 * GhostManager - central anti-detection manager for Quark.
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
    private boolean silentRotations = true;
    private boolean packetDelay = true;

    private final HumanizationEngine humanizer   = new HumanizationEngine();
    private final PacketShaper       packetShaper = new PacketShaper();

    // -------------------------------------------------------------------------
    // Profile enum
    // -------------------------------------------------------------------------

    public enum AntiCheatProfile {
        VANILLA,
        NCP,
        AAC,
        GRIM,
        WATCHDOG,
        SPARTAN,
        MATRIX,
        INTAVE,
        VERUS,
        KARHU,
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

    public void setSilentRotations(boolean silent) { this.silentRotations = silent; }
    public boolean isSilentRotations() { return silentRotations; }

    public void setPacketDelay(boolean delay) { this.packetDelay = delay; }
    public boolean isPacketDelay() { return packetDelay; }

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
            case NCP      -> AntiCheatBypass.NCP.getSafeSpeed(true);
            case AAC      -> AntiCheatBypass.AAC.getSafeSpeed();
            case GRIM     -> AntiCheatBypass.Grim.getSafeSpeed();
            case WATCHDOG -> AntiCheatBypass.Watchdog.getSafeSpeed();
            case SPARTAN  -> AntiCheatBypass.Spartan.getSafeSpeed();
            case MATRIX   -> AntiCheatBypass.Matrix.getSafeSpeed();
            case INTAVE   -> 0.29;
            case VERUS    -> AntiCheatBypass.Verus.getSafeSpeed();
            case KARHU    -> AntiCheatBypass.Karhu.getSafeSpeed();
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
            case GRIM     -> AntiCheatBypass.Grim.getMaxReach();
            case WATCHDOG -> AntiCheatBypass.Watchdog.getSafeReach();
            case SPARTAN  -> 3.2;
            case MATRIX   -> AntiCheatBypass.Matrix.getSafeReach();
            case INTAVE   -> 3.0;
            case VERUS    -> AntiCheatBypass.Verus.getSafeReach();
            case KARHU    -> AntiCheatBypass.Karhu.getSafeReach();
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
            case WATCHDOG -> (long) (AntiCheatBypass.Watchdog.getAttackCooldown() * 50L);
            case SPARTAN  -> 55L;
            case MATRIX   -> 65L;
            case INTAVE   -> 70L;
            case VERUS    -> AntiCheatBypass.Verus.getMinAttackDelay();
            case KARHU    -> AntiCheatBypass.Karhu.getMinAttackDelay();
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
            case VERUS    -> 22.0;
            case KARHU    -> 18.0;
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
            case GRIM, INTAVE, VERUS, KARHU -> false;
            default                         -> true;
        };
    }

    // -------------------------------------------------------------------------
    // Velocity modification
    // -------------------------------------------------------------------------

    /**
     * Returns a multiplier (0.0 â€“ 1.0) for incoming knockback velocity that
     * modules may apply without triggering velocity-reduction flags.
     *
     * <p>1.0 = full vanilla knockback (safe); 0.0 = no knockback (likely flagged).
     */
    public double getVelocityMultiplier() {
        return switch (activeProfile) {
            case VANILLA  -> 1.0;
            case NCP      -> AntiCheatBypass.NCP.useAcceleration() ? 0.6 : 0.8;
            case AAC      -> AntiCheatBypass.AAC.getAirControl();
            case GRIM     -> 1.0;
            case WATCHDOG -> 0.7;
            case SPARTAN  -> 0.65;
            case MATRIX   -> 0.75;
            case INTAVE   -> 0.8;
            case VERUS    -> 0.85;
            case KARHU    -> 0.9;
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
