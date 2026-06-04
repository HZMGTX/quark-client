package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PhaseWalk extends Module {

    private final BoolSetting active = register(new BoolSetting(
            "Active", "Enable phase-walk collision bypass", true));

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Movement speed while phasing", 0.1, 0.01, 1.0));

    private int phaseTick = 0;

    public PhaseWalk() {
        super("PhaseWalk", "Walk through thin walls (1 block)", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        phaseTick = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!active.isEnabled()) return;

        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) {
            phaseTick = 0;
            return;
        }

        float yaw = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        double len = Math.sqrt(fwd * fwd + side * side);
        double dx = (-Math.sin(yawRad) * (fwd / len) + Math.cos(yawRad) * (side / len));
        double dz = (Math.cos(yawRad) * (fwd / len) + Math.sin(yawRad) * (side / len));

        // Check if wall exists in movement direction
        Vec3d pos = mc.player.getPos();
        BlockPos frontPos = BlockPos.ofFloored(pos.add(dx * 0.6, 0, dz * 0.6));

        boolean wallAhead = !mc.world.getBlockState(frontPos).isAir();

        if (wallAhead) {
            // Alternate between two positions to clip through 1-block wall
            phaseTick++;
            double s = speed.get();
            if (phaseTick % 2 == 0) {
                // Phase position A: push slightly into wall
                mc.player.setVelocity(dx * s * 1.5, mc.player.getVelocity().y, dz * s * 1.5);
            } else {
                // Phase position B: normal speed
                mc.player.setVelocity(dx * s, mc.player.getVelocity().y, dz * s);
            }
        } else {
            phaseTick = 0;
        }
    }
}
