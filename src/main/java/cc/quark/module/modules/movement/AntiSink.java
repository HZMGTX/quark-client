package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AntiSink extends Module {

    public AntiSink() {
        super("AntiSink", "Prevents sinking when swimming near surface", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // Must be submerged in water
        BlockPos pos = mc.player.getBlockPos();
        FluidState fluidAtFeet = mc.world.getFluidState(pos);
        FluidState fluidAbove = mc.world.getFluidState(pos.up());

        boolean inWater = fluidAtFeet.isIn(FluidTags.WATER);
        boolean surfaceAbove = !fluidAbove.isIn(FluidTags.WATER);

        if (!inWater || !surfaceAbove) return;

        Vec3d vel = mc.player.getVelocity();

        // Cancel downward velocity when near surface of water
        if (vel.y < 0) {
            mc.player.setVelocity(vel.x, 0.0, vel.z);
        }

        // Keep player at water surface level
        double waterSurface = pos.getY() + fluidAtFeet.getHeight();
        double playerY = mc.player.getY();

        if (playerY < waterSurface - 0.2) {
            // Gently push up to surface
            mc.player.setVelocity(vel.x, 0.04, vel.z);
        }
    }
}
