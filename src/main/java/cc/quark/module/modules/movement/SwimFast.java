package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.Vec3d;

public class SwimFast extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Swim speed in liquids", 0.3, 0.05, 2.0));

    private final BoolSetting lava = register(new BoolSetting(
            "Lava", "Also apply speed boost in lava", false));

    public SwimFast() {
        super("SwimFast", "Swim faster in all liquids", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // Check if player is in a liquid
        FluidState fluid = mc.world.getFluidState(mc.player.getBlockPos());
        boolean inWater = fluid.isIn(FluidTags.WATER);
        boolean inLava = fluid.isIn(FluidTags.LAVA);

        if (!inWater && !(inLava && lava.isEnabled())) return;

        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        float yaw = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        double len = Math.sqrt(fwd * fwd + side * side);
        double dx = (-Math.sin(yawRad) * (fwd / len) + Math.cos(yawRad) * (side / len)) * speed.get();
        double dz = (Math.cos(yawRad) * (fwd / len) + Math.sin(yawRad) * (side / len)) * speed.get();

        Vec3d vel = mc.player.getVelocity();
        double motY = vel.y;

        // Upward when looking up / jumping
        if (mc.options.jumpKey.isPressed()) {
            motY = Math.min(0.2, motY + 0.05);
        }

        mc.player.setVelocity(dx, motY, dz);
    }
}
