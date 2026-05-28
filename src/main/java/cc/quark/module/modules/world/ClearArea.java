package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * ClearArea — breaks every non-air block within a cube around the player.
 *
 * Settings:
 *   Radius     — horizontal clear radius.
 *   Height     — vertical range (blocks above and below player).
 *   Keep Floor — skip the Y level the player is standing on.
 *   Auto Tool  — switch to the best tool for the current block.
 *   Delay      — milliseconds between break attempts.
 */
public class ClearArea extends Module {

    private final IntSetting  radius    = register(new IntSetting("Radius",     "Horizontal clear radius", 5,   1, 20));
    private final IntSetting  height    = register(new IntSetting("Height",     "Vertical clear height",   3,   1, 10));
    private final BoolSetting keepFloor = register(new BoolSetting("Keep Floor","Don't break the floor Y level", false));
    private final BoolSetting autoTool  = register(new BoolSetting("Auto Tool", "Switch to best tool automatically", true));
    private final IntSetting  delay     = register(new IntSetting("Delay",      "Milliseconds between breaks", 100, 0, 200));

    private final TimerUtil timer = new TimerUtil();
    private int remaining = 0;

    public ClearArea() {
        super("ClearArea", "Breaks every non-air block within a cube around you.", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @Override
    public String getSuffix() {
        return "Rem: " + remaining;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;
        timer.reset();

        int r = radius.get();
        int h = height.get();
        BlockPos center  = mc.player.getBlockPos();
        int floorY       = center.getY();

        // Count remaining and find nearest block
        remaining = 0;
        BlockPos target  = null;
        double  closest  = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.iterate(
                center.add(-r, -h, -r),
                center.add( r,  h,  r))) {

            BlockState state = mc.world.getBlockState(pos);
            if (state.isAir()) continue;
            if (state.getBlock() == Blocks.BEDROCK) continue;
            if (state.getHardness(mc.world, pos) < 0) continue; // unbreakable

            if (keepFloor.isEnabled() && pos.getY() == floorY) continue;

            remaining++;
            double dist = pos.getSquaredDistance(mc.player.getPos());
            if (dist < closest) {
                closest = dist;
                target  = pos.toImmutable();
            }
        }

        if (target != null) {
            if (autoTool.isEnabled()) selectBestTool(mc.world.getBlockState(target));
            mc.interactionManager.attackBlock(target, Direction.UP);
        }
    }

    private void selectBestTool(BlockState state) {
        if (mc.player == null) return;
        int   bestSlot  = -1;
        float bestSpeed = -1f;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot  = i;
            }
        }
        if (bestSlot != -1 && bestSlot != mc.player.getInventory().selectedSlot)
            mc.player.getInventory().selectedSlot = bestSlot;
    }
}
