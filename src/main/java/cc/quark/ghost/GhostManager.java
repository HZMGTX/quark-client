package cc.quark.ghost;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

public class GhostManager {

    public static final GhostManager INSTANCE = new GhostManager();

    private AntiCheatProfile activeProfile = AntiCheatProfile.GRIM;
    private boolean silentRotations = true;
    private boolean packetDelay     = true;
    private boolean autoDetect      = false;

    private int violationScore = 0;
    private static final int VIOLATION_MAX = 100;

    private final HumanizationEngine humanizer    = new HumanizationEngine();
    private final PacketShaper       packetShaper = new PacketShaper();

    public enum AntiCheatProfile {
        VANILLA,
        NCP,
        AAC,
        GRIM,
        WATCHDOG,
        SPARTAN,
        MATRIX,
        INTAVE,
        POLAR,
        VERUS,
        KARHU,
        CUSTOM
    }

    public AntiCheatProfile getActiveProfile() {
        return activeProfile;
    }

    public void setProfile(AntiCheatProfile profile) {
        this.activeProfile = profile;
    }

    public void setActiveProfile(AntiCheatProfile profile) {
        this.activeProfile = profile;
    }

    public void setSilentRotations(boolean silent)  { this.silentRotations = silent; }
    public boolean isSilentRotations()             { return silentRotations; }

    public void setPacketDelay(boolean delay)       { this.packetDelay = delay; }
    public boolean isPacketDelay()                 { return packetDelay; }

    public void setAutoDetect(boolean autoDetect)   { this.autoDetect = autoDetect; }
    public boolean isAutoDetect()                  { return autoDetect; }

    public void onTick() {
        if (autoDetect) {
            detectServer();
        }
    }

    private void detectServer() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ServerInfo info = mc.getCurrentServerEntry();
        if (info == null) return;

        String addr = info.address.toLowerCase();

        if (addr.contains("hypixel")) {
            activeProfile = AntiCheatProfile.WATCHDOG;
        } else if (addr.contains("mineplex")) {
            activeProfile = AntiCheatProfile.AAC;
        } else if (addr.contains("cubecraft")) {
            activeProfile = AntiCheatProfile.MATRIX;
        } else if (addr.contains("minemen") || addr.contains("mmc")) {
            activeProfile = AntiCheatProfile.GRIM;
        } else if (addr.contains("pvp.land") || addr.contains("intave")) {
            activeProfile = AntiCheatProfile.INTAVE;
        }
    }

    public void onChatMessage(String message) {
        if (message == null) return;
        String lower = message.toLowerCase();
        if (lower.contains("kicked") || lower.contains("flagged") ||
            lower.contains("cheat") || lower.contains("ban") ||
            lower.contains("violation")) {
            incrementViolationScore(15);
        }
        if (lower.contains("reconnect") || lower.contains("disconnect")) {
            incrementViolationScore(5);
        }
    }

    public void incrementViolationScore() {
        violationScore = Math.min(VIOLATION_MAX, violationScore + 1);
    }

    public void incrementViolationScore(int amount) {
        violationScore = Math.min(VIOLATION_MAX, violationScore + amount);
    }

    public void decrementViolationScore(int amount) {
        violationScore = Math.max(0, violationScore - amount);
    }

    public void resetViolationScore() {
        violationScore = 0;
    }

    public int getViolationScore() {
        return violationScore;
    }

    public boolean shouldThrottle() {
        return violationScore >= 50;
    }

    public boolean shouldThrottle(int threshold) {
        return violationScore >= threshold;
    }

    public double getMaxSpeed() {
        return switch (activeProfile) {
            case VANILLA  -> 1.0;
            case NCP      -> AntiCheatBypass.NCP.getSafeSpeed(true);
            case AAC      -> AntiCheatBypass.AAC.getSafeSpeed();
            case GRIM     -> AntiCheatBypass.Grim.getSafeSpeed();
            case WATCHDOG -> AntiCheatBypass.Watchdog.getSafeSpeed();
            case SPARTAN  -> AntiCheatBypass.Spartan.getSafeSpeed();
            case MATRIX   -> AntiCheatBypass.Matrix.getSafeSpeed();
            case INTAVE   -> AntiCheatBypass.Intave.getSafeSpeed();
            case POLAR    -> AntiCheatBypass.Polar.getSafeSpeed();
            case VERUS    -> AntiCheatBypass.Verus.getSafeSpeed();
            case KARHU    -> AntiCheatBypass.Karhu.getSafeSpeed();
            case CUSTOM   -> 0.35;
        };
    }

    public double getMaxReach() {
        return switch (activeProfile) {
            case VANILLA  -> 6.0;
            case NCP      -> 3.1;
            case AAC      -> 3.1;
            case GRIM     -> AntiCheatBypass.Grim.getMaxReach();
            case WATCHDOG -> AntiCheatBypass.Watchdog.getSafeReach();
            case SPARTAN  -> 3.2;
            case MATRIX   -> AntiCheatBypass.Matrix.getSafeReach();
            case INTAVE   -> AntiCheatBypass.Intave.getSafeReach();
            case POLAR    -> AntiCheatBypass.Polar.getSafeReach();
            case VERUS    -> AntiCheatBypass.Verus.getSafeReach();
            case KARHU    -> AntiCheatBypass.Karhu.getSafeReach();
            case CUSTOM   -> 3.5;
        };
    }

    public long getMinAttackDelay() {
        return switch (activeProfile) {
            case VANILLA  -> 0L;
            case NCP      -> 50L;
            case AAC      -> 60L;
            case GRIM     -> 75L;
            case WATCHDOG -> (long)(AntiCheatBypass.Watchdog.getAttackCooldown() * 50L);
            case SPARTAN  -> 55L;
            case MATRIX   -> 65L;
            case INTAVE   -> AntiCheatBypass.Intave.getMinAttackDelay();
            case POLAR    -> AntiCheatBypass.Polar.getMinAttackDelay();
            case VERUS    -> AntiCheatBypass.Verus.getMinAttackDelay();
            case KARHU    -> AntiCheatBypass.Karhu.getMinAttackDelay();
            case CUSTOM   -> 60L;
        };
    }

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
            case POLAR    -> AntiCheatBypass.Polar.getMaxRotationDelta();
            case VERUS    -> 22.0;
            case KARHU    -> 18.0;
            case CUSTOM   -> 40.0;
        };
    }

    public boolean canStrafeAttack() {
        return switch (activeProfile) {
            case GRIM, INTAVE, POLAR, VERUS, KARHU -> false;
            default                                 -> true;
        };
    }

    public double getVelocityMultiplier() {
        return switch (activeProfile) {
            case VANILLA  -> 1.0;
            case NCP      -> AntiCheatBypass.NCP.useAcceleration() ? 0.6 : 0.8;
            case AAC      -> AntiCheatBypass.AAC.getAirControl();
            case GRIM     -> 1.0;
            case WATCHDOG -> 0.7;
            case SPARTAN  -> 0.65;
            case MATRIX   -> 0.75;
            case INTAVE   -> AntiCheatBypass.Intave.getMaxVelocityReduction();
            case POLAR    -> 0.92;
            case VERUS    -> AntiCheatBypass.Verus.getMaxVelocityReduction();
            case KARHU    -> AntiCheatBypass.Karhu.getMaxVelocityReduction();
            case CUSTOM   -> 0.7;
        };
    }

    public HumanizationEngine getHumanizer() {
        return humanizer;
    }

    public PacketShaper getPacketShaper() {
        return packetShaper;
    }
}
