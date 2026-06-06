package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PhaseWalk extends Module {

    private final BoolSetting blocks = register(new BoolSetting(
            "Blocks", "Phase through 1-block-wide block gaps", true));
    private final BoolSetting entities = register(new BoolSetting(
            "Entities", "Reduce collision with entities while phasing", false));

    private int phaseTick = 0;

    public PhaseWalk() {
        super("PhaseWalk", "Lets player walk through 1-block-wide gaps by adjusting bounding box slightly", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        phaseTick = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc == null || mc.player == null || mc.world == null) return;
        if (!blocks.isEnabled()) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) { phaseTick = 0; return; }

        float yaw = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        double len = Math.sqrt(fwd * fwd + side * side);
        double dx = (-Math.sin(yawRad) * (fwd / len) + Math.cos(yawRad) * (side / len));
        double dz = ( Math.cos(yawRad) * (fwd / len) + Math.sin(yawRad) * (side / len));

        Vec3d pos = mc.player.getPos();
        BlockPos frontPos = BlockPos.ofFloored(pos.add(dx * 0.6, 0, dz * 0.6));
        boolean wallAhead = !mc.world.getBlockState(frontPos).isAir();

        if (wallAhead) {
            phaseTick++;
            if (phaseTick % 2 == 0) {
                mc.player.setVelocity(dx * 0.15, mc.player.getVelocity().y, dz * 0.15);
            } else {
                mc.player.setVelocity(dx * 0.08, mc.player.getVelocity().y, dz * 0.08);
            }
        } else {
            phaseTick = 0;
        }
    }
}
