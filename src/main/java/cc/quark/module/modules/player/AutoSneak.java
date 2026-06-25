package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoSneak extends Module {

    private final BoolSetting nearEdge = register(new BoolSetting(
            "NearEdge", "Only sneak when near a block edge", true));

    private final DoubleSetting edgeDistance = register(new DoubleSetting(
            "EdgeDistance", "Distance from edge to trigger sneaking (blocks)", 1.0, 0.1, 3.0));

    public AutoSneak() {
        super("AutoSneak", "Automatically sneaks near edges", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.options.sneakKey.setPressed(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (!nearEdge.isEnabled()) {
            // Always sneak
            mc.options.sneakKey.setPressed(true);
            return;
        }

        boolean shouldSneak = isNearEdge();
        mc.options.sneakKey.setPressed(shouldSneak);
    }

    private boolean isNearEdge() {
        if (mc.player == null || mc.world == null) return false;

        Vec3d pos = mc.player.getPos();
        double dist = edgeDistance.get();

        // Check the four cardinal directions for a missing block below
        double[][] offsets = {
            { dist, 0 },
            { -dist, 0 },
            { 0, dist },
            { 0, -dist }
        };

        for (double[] offset : offsets) {
            double checkX = pos.x + offset[0];
            double checkZ = pos.z + offset[1];
            BlockPos below = BlockPos.ofFloored(checkX, pos.y - 0.5, checkZ);
            if (!mc.world.getBlockState(below).isSolidBlock(mc.world, below)) {
                return true;
            }
        }
        return false;
    }
}
