package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class CrouchBoost extends Module {

    private final DoubleSetting boostAmount = register(new DoubleSetting(
            "BoostAmount", "Speed burst while crouching on ground", 0.3, 0.05, 1.0));

    private boolean wasCrouching = false;

    public CrouchBoost() {
        super("CrouchBoost", "Applies speed burst when crouching on ground", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasCrouching = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;

        boolean crouching = mc.player.isSneaking();
        boolean moving = mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;

        if (crouching && moving) {
            float yaw = (float) Math.toRadians(mc.player.getYaw());
            float fwd = mc.player.input.movementForward;
            float side = mc.player.input.movementSideways;
            double len = Math.sqrt(fwd * fwd + side * side);
            double nf = fwd / len;
            double ns = side / len;

            double vx = (-Math.sin(yaw) * nf + Math.cos(yaw) * ns) * boostAmount.get();
            double vz = (Math.cos(yaw) * nf + Math.sin(yaw) * ns) * boostAmount.get();

            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vx, vel.y, vz);
        }

        wasCrouching = crouching;
    }
}
