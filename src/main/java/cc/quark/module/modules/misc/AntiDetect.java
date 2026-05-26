package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventTick;
import cc.quark.ghost.GhostManager;
import cc.quark.ghost.RotationManager;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.EnumSetting;
import cc.quark.setting.IntSetting;

public class AntiDetect extends Module {

    public enum ACProfile {
        VANILLA, NCP, AAC, GRIM, WATCHDOG, SPARTAN, MATRIX, INTAVE, POLAR, VERUS, KARHU, CUSTOM
    }

    private final EnumSetting<ACProfile> profile = register(new EnumSetting<>(
            "Profile", "Anti-cheat server to bypass", ACProfile.GRIM));

    private final BoolSetting autoProfile = register(new BoolSetting(
            "Auto Profile", "Automatically detect and set AC profile based on server address", false));

    private final BoolSetting silentRotations = register(new BoolSetting(
            "Silent Rotations", "Use server-side only rotations (head turns server-side, body stays)", true));

    private final BoolSetting packetDelay = register(new BoolSetting(
            "Packet Delay", "Add humanized delay to outgoing packets", true));

    private final BoolSetting velocityJitter = register(new BoolSetting(
            "Velocity Jitter", "Add tiny random jitter to velocity for natural movement", false));

    private final IntSetting jitterAmount = register(new IntSetting(
            "Jitter Amount", "Velocity jitter magnitude (x0.001 blocks/tick)", 3, 1, 10));

    private final BoolSetting clickHumanize = register(new BoolSetting(
            "Click Humanize", "Pass click timing through ClickSimulator for humanized CPS", false));

    private final BoolSetting gcdFix = register(new BoolSetting(
            "GCD Fix", "Apply GCD mouse quantization fix to rotations", false));

    private final IntSetting violationThreshold = register(new IntSetting(
            "Violation Threshold", "Auto-throttle modules when violation score exceeds this", 60, 10, 100));

    private final BoolSetting timerSpoof = register(new BoolSetting(
            "Timer Spoof", "Slightly vary tick timing to defeat timer detection", false));

    public AntiDetect() {
        super("AntiDetect", "Configures global anti-cheat evasion profile", Category.MISC);
    }

    @Override
    public void onEnable() {
        syncAll();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        syncAll();

        if (velocityJitter.isEnabled()) {
            double jitter = jitterAmount.get() * 0.001;
            double[] noise = GhostManager.INSTANCE.getHumanizer().generateMovementNoise();
            double dx = noise[0] * (jitterAmount.get() / 3.0);
            double dz = noise[1] * (jitterAmount.get() / 3.0);
            mc.player.addVelocity(dx, 0, dz);
        }

        GhostManager ghost = GhostManager.INSTANCE;
        ghost.onTick();

        if (ghost.shouldThrottle(violationThreshold.get())) {
            ghost.decrementViolationScore(1);
        }
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (event.isIncoming()) {
            GhostManager.INSTANCE.onChatMessage(event.getMessage());
        }
    }

    private void syncAll() {
        GhostManager ghost = GhostManager.INSTANCE;

        ghost.setAutoDetect(autoProfile.isEnabled());

        if (!autoProfile.isEnabled()) {
            GhostManager.AntiCheatProfile ghostProfile = switch (profile.get()) {
                case VANILLA  -> GhostManager.AntiCheatProfile.VANILLA;
                case NCP      -> GhostManager.AntiCheatProfile.NCP;
                case AAC      -> GhostManager.AntiCheatProfile.AAC;
                case GRIM     -> GhostManager.AntiCheatProfile.GRIM;
                case WATCHDOG -> GhostManager.AntiCheatProfile.WATCHDOG;
                case SPARTAN  -> GhostManager.AntiCheatProfile.SPARTAN;
                case MATRIX   -> GhostManager.AntiCheatProfile.MATRIX;
                case INTAVE   -> GhostManager.AntiCheatProfile.INTAVE;
                case POLAR    -> GhostManager.AntiCheatProfile.POLAR;
                case VERUS    -> GhostManager.AntiCheatProfile.VERUS;
                case KARHU    -> GhostManager.AntiCheatProfile.KARHU;
                case CUSTOM   -> GhostManager.AntiCheatProfile.CUSTOM;
            };
            ghost.setProfile(ghostProfile);
        }

        ghost.setSilentRotations(silentRotations.isEnabled());
        ghost.setPacketDelay(packetDelay.isEnabled());

        RotationManager.INSTANCE.setGcdFix(gcdFix.isEnabled());
        RotationManager.INSTANCE.setSilent(silentRotations.isEnabled());
    }
}
