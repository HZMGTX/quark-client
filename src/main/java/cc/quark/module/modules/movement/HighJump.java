package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventJump;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * HighJump - multiplies the player's jump height by increasing the upward velocity
 * applied at the moment of the jump.
 *
 * <p>Vanilla jump velocity is {@code 0.42} blocks/tick.  We replace it with
 * {@code 0.42 * height} so a height of 2.0 doubles the jump height.
 */
public class HighJump extends Module {

    /** Vanilla initial jump Y-velocity. */
    private static final double VANILLA_JUMP_VELOCITY = 0.42;

    private final DoubleSetting height = register(new DoubleSetting(
            "Height", "Jump height multiplier (1 = vanilla)", 2.0, 1.0, 10.0));

    private final cc.quark.setting.BoolSetting doubleJump = register(new cc.quark.setting.BoolSetting(
            "Double Jump", "Allow a second jump mid-air by pressing space again", false));

    private final cc.quark.setting.BoolSetting noFallDamage = register(new cc.quark.setting.BoolSetting(
            "No Fall Damage", "Send onGround=true packet to cancel fall damage", true));

    private boolean canDoubleJump = false;
    private boolean wasSpaceDown = false;

    public HighJump() {
        super("HighJump", "Multiplies jump height with optional double-jump", Category.MOVEMENT);
    }

    @Override
    public String getSuffix() { return String.format("%.1fx", height.get()); }

    @Override
    public void onEnable() { canDoubleJump = false; wasSpaceDown = false; }

    @EventHandler
    public void onJump(EventJump event) {
        if (mc.player == null) return;
        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x, VANILLA_JUMP_VELOCITY * height.get(), vel.z);
        canDoubleJump = doubleJump.isEnabled();
    }

    @EventHandler
    public void onTick(cc.quark.event.events.EventTick event) {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();
        boolean spaceDown = mc.options.jumpKey.isPressed();

        if (onGround) { canDoubleJump = false; }

        // Double jump: detect new space press while airborne and can still double-jump
        if (!onGround && spaceDown && !wasSpaceDown && canDoubleJump) {
            canDoubleJump = false;
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, VANILLA_JUMP_VELOCITY * height.get() * 0.7, vel.z);
        }

        // No fall damage via packet spoof
        if (noFallDamage.isEnabled() && !onGround && mc.player.fallDistance > 2.5f
                && mc.player.getVelocity().y < -0.4) {
            mc.player.networkHandler.sendPacket(
                new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    mc.player.getYaw(), mc.player.getPitch(), true));
            mc.player.fallDistance = 0;
        }

        wasSpaceDown = spaceDown;
    }
}
