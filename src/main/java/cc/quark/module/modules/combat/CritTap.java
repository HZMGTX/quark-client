package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * CritTap - Performs a mini tap-jump to guarantee critical hits without
 * visibly leaving the ground. Sends a short up-down packet sequence so
 * the server registers the player as airborne during the attack.
 */
public class CritTap extends Module {

    private final BoolSetting onlyOnAttack = register(new BoolSetting(
            "Only On Attack", "Only tap-jump when a target is in sight", true));

    private final IntSetting packetDelay = register(new IntSetting(
            "Packet Delay", "Ticks between tap-jump packets", 2, 1, 10));

    private final BoolSetting requireSprint = register(new BoolSetting(
            "Require Sprint", "Only trigger when sprinting", false));

    private int tickTimer = 0;
    private int phase = 0; // 0 = idle, 1 = sent up, 2 = sent down

    public CritTap() {
        super("CritTap", "Tap-jumps via packets to get critical hits without moving", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        phase = 0;
        tickTimer = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (!mc.player.isOnGround()) return;
        if (requireSprint.isEnabled() && !mc.player.isSprinting()) return;

        if (onlyOnAttack.isEnabled()) {
            if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) {
                phase = 0;
                return;
            }
            Entity target = ((EntityHitResult) mc.crosshairTarget).getEntity();
            if (!(target instanceof LivingEntity)) {
                phase = 0;
                return;
            }
        }

        tickTimer++;
        if (tickTimer < packetDelay.get()) return;
        tickTimer = 0;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();

        switch (phase) {
            case 0 -> {
                // Send a packet slightly above current position (airborne)
                mc.getNetworkHandler().sendPacket(
                        new PlayerMoveC2SPacket.Full(x, y + 0.0625, z, yaw, pitch, false));
                phase = 1;
            }
            case 1 -> {
                // Send a packet back to ground position
                mc.getNetworkHandler().sendPacket(
                        new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, true));
                phase = 0;
            }
        }
    }
}
