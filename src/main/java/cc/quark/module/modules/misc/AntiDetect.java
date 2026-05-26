package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.ghost.GhostManager;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.EnumSetting;
import cc.quark.setting.IntSetting;

public class AntiDetect extends Module {

    public enum ACProfile {
        VANILLA, NCP, AAC, GRIM, WATCHDOG, SPARTAN, MATRIX, INTAVE, VERUS, KARHU, CUSTOM
    }

    private final EnumSetting<ACProfile> profile = register(new EnumSetting<>(
            "Profile", "Anti-cheat server to bypass", ACProfile.GRIM));

    private final BoolSetting silentRotations = register(new BoolSetting(
            "Silent Rotations", "Use server-side only rotations (head turns server-side, body stays)", true));

    private final BoolSetting packetDelay = register(new BoolSetting(
            "Packet Delay", "Add humanized delay to outgoing packets", true));

    private final BoolSetting velocityJitter = register(new BoolSetting(
            "Velocity Jitter", "Add tiny random jitter to velocity for natural movement", false));

    private final IntSetting jitterAmount = register(new IntSetting(
            "Jitter Amount", "Velocity jitter magnitude (×0.001 blocks/tick)", 3, 1, 10));

    private final BoolSetting timerSpoof = register(new BoolSetting(
            "Timer Spoof", "Slightly vary tick timing to defeat timer detection", false));

    public AntiDetect() {
        super("AntiDetect", "Configures global anti-cheat evasion profile", Category.MISC);
    }

    @Override
    public void onEnable() {
        syncProfile();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        syncProfile();

        if (velocityJitter.isEnabled()) {
            double jitter = jitterAmount.get() * 0.001;
            double dx = (Math.random() - 0.5) * jitter;
            double dz = (Math.random() - 0.5) * jitter;
            mc.player.addVelocity(dx, 0, dz);
        }
    }

    private void syncProfile() {
        GhostManager.AntiCheatProfile ghostProfile = switch (profile.get()) {
            case VANILLA  -> GhostManager.AntiCheatProfile.VANILLA;
            case NCP      -> GhostManager.AntiCheatProfile.NCP;
            case AAC      -> GhostManager.AntiCheatProfile.AAC;
            case GRIM     -> GhostManager.AntiCheatProfile.GRIM;
            case WATCHDOG -> GhostManager.AntiCheatProfile.WATCHDOG;
            case SPARTAN  -> GhostManager.AntiCheatProfile.SPARTAN;
            case MATRIX   -> GhostManager.AntiCheatProfile.MATRIX;
            case INTAVE   -> GhostManager.AntiCheatProfile.INTAVE;
            case VERUS    -> GhostManager.AntiCheatProfile.VERUS;
            case KARHU    -> GhostManager.AntiCheatProfile.KARHU;
            case CUSTOM   -> GhostManager.AntiCheatProfile.CUSTOM;
        };
        GhostManager.INSTANCE.setProfile(ghostProfile);
        GhostManager.INSTANCE.setSilentRotations(silentRotations.isEnabled());
        GhostManager.INSTANCE.setPacketDelay(packetDelay.isEnabled());
    }
}
