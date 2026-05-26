package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.BlockPos;

public class SafeWalk extends Module {

    private final BoolSetting onlyOnGround = register(new BoolSetting(
            "Only On Ground", "Only prevent walking off edges when on the ground", true));

    public SafeWalk() {
        super("SafeWalk", "Prevents walking off the edges of blocks", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null || mc.world == null) return;

        if (onlyOnGround.isEnabled() && !mc.player.isOnGround()) return;

        if (event.getX() == 0 && event.getZ() == 0) return;

        double nextX = mc.player.getX() + event.getX();
        double nextZ = mc.player.getZ() + event.getZ();
        double currentY = mc.player.getY();

        BlockPos belowNext = new BlockPos(
                (int) Math.floor(nextX),
                (int) Math.floor(currentY) - 1,
                (int) Math.floor(nextZ));

        if (mc.world.getBlockState(belowNext).isAir()) {
            event.setX(0);
            event.setZ(0);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (onlyOnGround.isEnabled() && !mc.player.isOnGround()) return;

        double vx = mc.player.getVelocity().x;
        double vz = mc.player.getVelocity().z;

        if (vx == 0 && vz == 0) return;

        double nextX = mc.player.getX() + vx;
        double nextZ = mc.player.getZ() + vz;
        double currentY = mc.player.getY();

        BlockPos belowNext = new BlockPos(
                (int) Math.floor(nextX),
                (int) Math.floor(currentY) - 1,
                (int) Math.floor(nextZ));

        if (mc.world.getBlockState(belowNext).isAir()) {
            mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
        }
    }
}
