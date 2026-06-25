package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * AntiCrit2 — prevents enemy critical hits by keeping the local player grounded.
 *
 * <p>Enemies can only land a critical hit when the target is in the air (falling).
 * This module periodically sends {@link PlayerMoveC2SPacket.OnGroundOnly} packets
 * with {@code onGround = true}, convincing the server that the player is always
 * on the ground, and optionally cancels any upward velocity to prevent the player
 * from being sent airborne by knockback.
 */
public class AntiCrit2 extends Module {

    private final BoolSetting groundPackets = register(new BoolSetting(
            "Ground Packets", "Send on-ground packets every interval to prevent server-side air state", true));

    private final IntSetting packetInterval = register(new IntSetting(
            "Packet Interval", "Ticks between ground-spoof packets (lower = more aggressive)", 5, 1, 20));

    private final BoolSetting cancelAirVelocity = register(new BoolSetting(
            "Cancel Air Velocity", "Zero out upward velocity when not already on ground", false));

    private int tickCounter = 0;

    public AntiCrit2() {
        super("AntiCrit2", "Prevents enemy critical hits by keeping the player grounded", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        tickCounter++;

        // Optionally cancel upward velocity so knockback cannot send the player airborne
        if (cancelAirVelocity.isEnabled() && !mc.player.isOnGround()) {
            double vy = mc.player.getVelocity().y;
            if (vy > 0) {
                mc.player.setVelocity(mc.player.getVelocity().x, 0, mc.player.getVelocity().z);
            }
        }

        // Periodically send on-ground packets to spoof server-side ground state
        if (groundPackets.isEnabled() && tickCounter >= packetInterval.get()) {
            tickCounter = 0;
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, mc.player.horizontalCollision));
        }
    }
}
