package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.EnumSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * Criticals - ensures every hit is a critical hit by manipulating the player's vertical position
 * or by sending fake packets that trick the server into registering a crit.
 *
 * <p>Modes:
 * <ul>
 *   <li><b>Packet</b> - sends position packets slightly above ground so the server thinks the
 *       player is falling, registering a critical hit without any visible movement.</li>
 *   <li><b>Jump</b> - makes the player jump immediately before the attack.</li>
 *   <li><b>MinJump</b> - applies a tiny upward velocity (0.1 blocks) just before the attack,
 *       which is enough to trigger the crit flag with minimal visual displacement.</li>
 * </ul>
 */
public class Criticals extends Module {

    public enum CritMode {
        PACKET, JUMP, MIN_JUMP
    }

    private final EnumSetting<CritMode> mode = register(new EnumSetting<>(
            "Mode", "Method used to trigger critical hits", CritMode.PACKET));

    public Criticals() {
        super("Criticals", "Makes every hit a critical hit", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        // Must be on ground or falling to produce a crit; we mimic the required state.
        switch (mode.get()) {
            case PACKET -> doPacketCrit();
            case JUMP   -> doJumpCrit();
            case MIN_JUMP -> doMinJumpCrit();
        }
    }

    /**
     * Sends three position packets:
     * <ol>
     *   <li>Current position + 0.11 (jumping slightly)</li>
     *   <li>Current position + 0.1 (just above ground, falling)</li>
     *   <li>Current position (landing, triggers server-side crit check)</li>
     * </ol>
     * The server sees the player briefly off the ground and then landing, which satisfies the
     * critical hit condition (falling and not on ground when the attack registers).
     */
    private void doPacketCrit() {
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        // Send fake offsets to convince the server the player is falling
        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.11, z, false));
        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.1, z, false));
        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
    }

    /**
     * Causes the player to jump.  The attack event fires immediately after so the hit
     * occurs while the player is ascending (or at peak), satisfying the crit condition
     * on the next tick when falling begins.
     */
    private void doJumpCrit() {
        if (mc.player.isOnGround()) {
            mc.player.jump();
        }
    }

    /**
     * Applies a tiny upward velocity (0.1) which is just enough to take the player off
     * the ground and trigger the server-side "was falling" crit check without any
     * perceptible jump.
     */
    private void doMinJumpCrit() {
        if (mc.player.isOnGround()) {
            // Set a small upward velocity; enough to register crit, not enough to visibly jump
            mc.player.setVelocity(mc.player.getVelocity().x, 0.1, mc.player.getVelocity().z);
        }
    }
}
