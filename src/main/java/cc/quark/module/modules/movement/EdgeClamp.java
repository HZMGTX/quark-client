package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EdgeClamp extends Module {

    private final BoolSetting enabled = register(new BoolSetting(
            "Enabled", "Stop movement when about to walk off an edge", true));

    public EdgeClamp() {
        super("EdgeClamp", "Prevents player from falling off edges by stopping movement at block edge", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc == null || mc.player == null || mc.world == null) return;
        if (!enabled.isEnabled()) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.isSneaking()) return;

        Vec3d vel = mc.player.getVelocity();
        if (Math.abs(vel.x) < 0.001 && Math.abs(vel.z) < 0.001) return;

        double nx = mc.player.getX() + vel.x * 2.0;
        double nz = mc.player.getZ() + vel.z * 2.0;
        double y  = mc.player.getY();

        BlockPos below = new BlockPos(
                (int) Math.floor(nx),
                (int) Math.floor(y) - 1,
                (int) Math.floor(nz));

        if (mc.world.getBlockState(below).isAir()) {
            mc.player.setVelocity(0, vel.y, 0);
        }
    }
}
