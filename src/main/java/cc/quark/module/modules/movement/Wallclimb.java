package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Wallclimb - climb any block face; when pressing forward against a wall,
 * apply upward velocity. NoFall option resets fall distance to avoid damage.
 */
public class Wallclimb extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Climb speed (blocks/tick)", 0.2, 0.05, 0.5));
    private final BoolSetting noFall = register(new BoolSetting(
            "No Fall", "Reset fall distance while climbing", true));

    public Wallclimb() {
        super("Wallclimb", "Climb any wall face by pressing forward while touching it", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.input.movementForward <= 0) return;

        Direction facing = mc.player.getHorizontalFacing();
        BlockPos front   = mc.player.getBlockPos().offset(facing);

        if (mc.world.getBlockState(front).isAir()) return;

        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x, speed.get(), v.z);

        if (noFall.isEnabled()) {
            mc.player.fallDistance = 0;
        }
    }
}
