package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.Vec3d;

public class SwimSprint extends Module {

    private final BoolSetting inLava = register(new BoolSetting("InLava", "Apply speed boost while in lava as well", false));

    public SwimSprint() {
        super("SwimSprint", "Applies sprint speed while swimming regardless of food level", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean inWater = mc.player.isTouchingWater();
        boolean inLavaCondition = inLava.isEnabled() && mc.player.isInLava();

        if (!inWater && !inLavaCondition) return;

        mc.player.setSprinting(true);

        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        double yawRad = Math.toRadians(mc.player.getYaw());
        double len = Math.sqrt(fwd * fwd + side * side);
        double nx = fwd / len;
        double nz = side / len;

        double vx = (-Math.sin(yawRad) * nx + Math.cos(yawRad) * nz) * 0.26;
        double vz = ( Math.cos(yawRad) * nx + Math.sin(yawRad) * nz) * 0.26;

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vx, vel.y, vz);
    }
}
