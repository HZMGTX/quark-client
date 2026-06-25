package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AntiWall2 extends Module {

    private final BoolSetting autoEscape = register(new BoolSetting(
            "Auto Escape", "Automatically move out of walls", true));

    public AntiWall2() {
        super("AntiWall2", "Prevents getting stuck in walls", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos pos = mc.player.getBlockPos();
        boolean insideBlock = !mc.world.getBlockState(pos).isAir()
                || !mc.world.getBlockState(pos.up()).isAir();

        if (insideBlock && autoEscape.isEnabled()) {
            // Push the player upward to escape
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, 0.25, vel.z);
        }
    }
}
