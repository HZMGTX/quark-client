package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * StrafeSpeed - boosts horizontal strafe velocity while the player is airborne.
 *
 * <p>In Minecraft, air strafing is limited by vanilla's {@code 0.02} air
 * acceleration constant.  This module supplements it by directly applying
 * additional velocity in the strafe direction each tick while airborne.
 */
public class StrafeSpeed extends Module {

    private final DoubleSetting boostAmount = register(new DoubleSetting(
            "Boost", "Extra velocity added per tick in the strafe direction", 0.05, 0.01, 0.3));

    private final DoubleSetting maxSpeed = register(new DoubleSetting(
            "Max Speed", "Maximum horizontal speed cap (blocks/tick, 0 = no cap)", 0.5, 0.0, 2.0));

    private final BoolSetting onlySideways = register(new BoolSetting(
            "Only Sideways", "Only boost purely sideways movement, not diagonal", false));

    private final BoolSetting requireAir = register(new BoolSetting(
            "Require Air", "Only apply boost while airborne", true));

    public StrafeSpeed() {
        super("StrafeSpeed", "Boosts strafe speed while airborne for sharper air movement", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean inAir = !mc.player.isOnGround();
        if (requireAir.isEnabled() && !inAir) return;

        float side = mc.player.input.movementSideways;
        float fwd  = mc.player.input.movementForward;

        if (side == 0) return;
        if (onlySideways.isEnabled() && fwd != 0) return;

        float yaw = (float) Math.toRadians(mc.player.getYaw());

        // Sideways direction in world space
        double strafeX = Math.cos(yaw) * side;
        double strafeZ = Math.sin(yaw) * side;
        double len = Math.sqrt(strafeX * strafeX + strafeZ * strafeZ);
        if (len > 0) {
            strafeX /= len;
            strafeZ /= len;
        }

        Vec3d vel = mc.player.getVelocity();
        double newX = vel.x + strafeX * boostAmount.get();
        double newZ = vel.z + strafeZ * boostAmount.get();

        // Apply speed cap if configured
        double cap = maxSpeed.get();
        if (cap > 0) {
            double hSpeed = Math.sqrt(newX * newX + newZ * newZ);
            if (hSpeed > cap) {
                double scale = cap / hSpeed;
                newX *= scale;
                newZ *= scale;
            }
        }

        mc.player.setVelocity(newX, vel.y, newZ);
    }
}
