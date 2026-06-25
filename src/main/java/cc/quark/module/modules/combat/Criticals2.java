package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.EnumSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * Criticals2 — guarantees every melee hit is a critical hit via configurable exploit modes.
 *
 * <p>Critical hits require the player to be falling (not on the ground, positive downward
 * velocity, not in water, etc.). This module replicates the falling state artificially
 * just before each attack.
 *
 * <p>Modes:
 * <ul>
 *   <li><b>Packet</b> — sends three position packets (y+0.11, y+0.1, y+0.0) with
 *       {@code onGround=false}, convincing the server the player jumped and landed
 *       without any client-side movement. Bypasses most basic anti-cheats.</li>
 *   <li><b>MicroJump</b> — applies a tiny upward velocity (configurable, default 0.1 blocks)
 *       so the player physically leaves the ground for one tick before the hit lands.
 *       Visible but hard to detect as a hack.</li>
 *   <li><b>Jump</b> — makes the player perform a full jump before attacking.
 *       Most visible but also most reliable on strict servers.</li>
 * </ul>
 */
public class Criticals2 extends Module {

    public enum Mode {
        PACKET, MICRO_JUMP, JUMP
    }

    private final EnumSetting<Mode> mode = register(new EnumSetting<>(
            "Mode", "Technique used to trigger critical hits", Mode.PACKET));

    private final DoubleSetting microJumpStrength = register(new DoubleSetting(
            "Micro-Jump Strength", "Upward velocity applied in MicroJump mode", 0.1, 0.01, 0.4));

    private final BoolSetting cooldownCheck = register(new BoolSetting(
            "Cooldown Check", "Only trigger crit when attack cooldown is fully charged", true));

    public Criticals2() {
        super("Criticals2", "Enhances every attack to a critical hit via micro-jump packets", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        // Optionally skip if the attack bar is not fully charged (avoids wasted crits)
        if (cooldownCheck.isEnabled()) {
            if (mc.player.getAttackCooldownProgress(0.0f) < 1.0f) return;
        }

        switch (mode.get()) {
            case PACKET     -> doPacketCrit();
            case MICRO_JUMP -> doMicroJump();
            case JUMP       -> doJump();
        }
    }

    /**
     * Sends fake position packets that convince the server the player briefly left
     * the ground and landed, satisfying the critical-hit condition without any
     * visible movement. Packet order: y+0.11 (air), y+0.1 (air, falling), y+0 (landing).
     */
    private void doPacketCrit() {
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.11, z, false));
        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.1,  z, false));
        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(x, y,        z, false));
    }

    /**
     * Applies a small configurable upward velocity. The player will visibly lift
     * off the ground by a fraction of a block before falling back down.
     */
    private void doMicroJump() {
        if (mc.player.isOnGround()) {
            mc.player.setVelocity(
                    mc.player.getVelocity().x,
                    microJumpStrength.get(),
                    mc.player.getVelocity().z);
        }
    }

    /**
     * Triggers a full vanilla jump. The attack event fires during the ascent phase;
     * the server records the crit on the subsequent falling tick.
     */
    private void doJump() {
        if (mc.player.isOnGround()) {
            mc.player.jump();
        }
    }
}
