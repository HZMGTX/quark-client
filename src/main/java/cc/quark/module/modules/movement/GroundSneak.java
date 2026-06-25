package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class GroundSneak extends Module {

    private final IntSetting lookAheadBlocks = register(new IntSetting(
            "LookAheadBlocks", "Blocks ahead to check for edges", 1, 1, 3));

    public GroundSneak() {
        super("GroundSneak", "Auto-sneaks when near edge of block to prevent falling", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;

        if (isNearEdge()) {
            // Force sneak input to be recognized by the server
            mc.player.setSneaking(true);
        }
    }

    private boolean isNearEdge() {
        int lookAhead = lookAheadBlocks.get();
        Vec3d pos = mc.player.getPos();
        float yaw = (float) Math.toRadians(mc.player.getYaw());

        for (int i = 1; i <= lookAhead; i++) {
            double checkX = pos.x + (-Math.sin(yaw) * i * 0.4);
            double checkZ = pos.z + (Math.cos(yaw) * i * 0.4);
            BlockPos checkPos = new BlockPos((int) Math.floor(checkX), (int) Math.floor(pos.y) - 1, (int) Math.floor(checkZ));

            if (mc.world.getBlockState(checkPos).isAir()) {
                return true;
            }
        }
        return false;
    }
}
