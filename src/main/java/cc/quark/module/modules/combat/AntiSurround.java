package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * AntiSurround - detects when a nearby enemy player is surrounded and breaks their surround blocks.
 */
public class AntiSurround extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to find target players", 5.0, 2.0, 6.0));

    private final BoolSetting breakObsidian = register(new BoolSetting(
            "Break Obsidian", "Prefer breaking obsidian blocks in the surround", true));

    private final BoolSetting autoTool = register(new BoolSetting(
            "Auto Tool", "Auto-switch to best tool for breaking surround blocks", true));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between break actions", 50, 0, 500));

    private final TimerUtil timer = new TimerUtil();

    public AntiSurround() {
        super("AntiSurround", "Breaks surround blocks placed around nearby players", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Find the nearest enemy player within range
        PlayerEntity target = findNearestTarget();
        if (target == null) return;

        BlockPos targetBase = target.getBlockPos();
        Direction[] horizontals = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

        // Look for surround blocks adjacent to the target
        BlockPos bestBreak = null;
        float bestHardness = Float.MAX_VALUE;

        for (Direction dir : horizontals) {
            BlockPos neighborPos = targetBase.offset(dir);
            if (mc.world.getBlockState(neighborPos).isAir()) continue;
            if (!mc.world.getBlockState(neighborPos).isSolidBlock(mc.world, neighborPos)) continue;

            float hardness = mc.world.getBlockState(neighborPos).getHardness(mc.world, neighborPos);
            if (hardness < 0) continue; // unbreakable (e.g. bedrock)

            // Prefer obsidian if setting enabled
            if (breakObsidian.isEnabled() &&
                    mc.world.getBlockState(neighborPos).getBlock() == Blocks.OBSIDIAN) {
                bestBreak = neighborPos;
                bestHardness = hardness;
                break;
            }

            if (hardness < bestHardness) {
                bestHardness = hardness;
                bestBreak = neighborPos;
            }
        }

        if (bestBreak == null) return;

        // Auto-switch to best tool if enabled
        int prevSlot = mc.player.getInventory().selectedSlot;
        if (autoTool.isEnabled()) {
            int toolSlot = findBestToolSlot(bestBreak);
            if (toolSlot != -1) {
                mc.player.getInventory().selectedSlot = toolSlot;
            }
        }

        // Break the block
        mc.interactionManager.attackBlock(bestBreak, Direction.UP);
        mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
        timer.reset();

        // Restore slot if auto-tool was used
        if (autoTool.isEnabled()) {
            mc.player.getInventory().selectedSlot = prevSlot;
        }
    }

    private PlayerEntity findNearestTarget() {
        PlayerEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (net.minecraft.entity.Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            if (player == mc.player) continue;
            if (player.isDead() || player.getHealth() <= 0) continue;
            double dist = mc.player.distanceTo(player);
            if (dist <= range.get() && dist < nearestDist) {
                nearestDist = dist;
                nearest = player;
            }
        }
        return nearest;
    }

    private int findBestToolSlot(BlockPos pos) {
        if (mc.player == null) return -1;
        float bestSpeed = -1f;
        int bestSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            float speed = 0f;
            if (stack.getItem() instanceof PickaxeItem pick) {
                speed = pick.getMaterial().getMiningSpeedMultiplier();
                // Pickaxes are best for obsidian
                speed += 5f;
            } else if (stack.getItem() instanceof AxeItem axe) {
                speed = axe.getMaterial().getMiningSpeedMultiplier();
            }
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        return bestSlot;
    }
}
