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
 * Excavator — automatically mines a rectangular tunnel in the direction the
 * player is facing.
 *
 * Settings:
 *   Width     — half-width of the tunnel (total = 2*W+1 blocks wide).
 *   Height    — height of the tunnel in blocks.
 *   Depth     — how many blocks ahead to mine.
 *   Auto Tool — switch to the best tool in the hotbar for the current block.
 *   Delay     — milliseconds between break attempts.
 */
public class Excavator extends Module {

    private final IntSetting  width    = register(new IntSetting("Width",     "Tunnel half-width (total = 2*W+1)", 2,  1, 10));
    private final IntSetting  height   = register(new IntSetting("Height",    "Tunnel height in blocks",           3,  1, 20));
    private final IntSetting  depth    = register(new IntSetting("Depth",     "Blocks ahead to mine",              10, 1, 50));
    private final BoolSetting autoTool = register(new BoolSetting("Auto Tool","Switch to best tool automatically", true));
    private final IntSetting  delay    = register(new IntSetting("Delay",     "Milliseconds between breaks",       50, 0, 200));

    private final TimerUtil timer  = new TimerUtil();
    private int brokenCount = 0;

    public Excavator() {
        super("Excavator", "Mines a tunnel in the direction you face.", Category.WORLD);
    }

    @Override
    public void onEnable() {
        brokenCount = 0;
        timer.reset();
    }

    @Override
    public String getSuffix() {
        return "Broken: " + brokenCount;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;
        timer.reset();

        BlockPos origin  = mc.player.getBlockPos();
        Direction facing = mc.player.getHorizontalFacing();
        int w = width.get();
        int h = height.get();
        int d = depth.get();

        for (int fw = 1; fw <= d; fw++) {
            for (int dx = -w; dx <= w; dx++) {
                for (int dy = 0; dy < h; dy++) {
                    BlockPos target = origin
                            .offset(facing, fw)
                            .offset(facing.rotateYClockwise(), dx)
                            .up(dy);

                    BlockState state = mc.world.getBlockState(target);
                    if (state.isAir()) continue;
                    if (state.getBlock() == Blocks.BEDROCK) continue;
                    if (state.getHardness(mc.world, target) < 0) continue; // unbreakable

                    if (autoTool.isEnabled()) selectBestTool(state);

                    mc.interactionManager.attackBlock(target, Direction.UP);
                    brokenCount++;
                    return;
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Tool selection — uses getMiningSpeedMultiplier like Nuker.java
    // -------------------------------------------------------------------------

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
        if (bestSlot != -1 && bestSlot != mc.player.getInventory().selectedSlot) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }
}
