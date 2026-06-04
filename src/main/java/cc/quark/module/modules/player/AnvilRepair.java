package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AnvilRepair extends Module {

    private final DoubleSetting durability = register(new DoubleSetting(
            "Durability", "Durability fraction threshold to trigger repair (0-1)", 0.2, 0.01, 1.0));
    private final BoolSetting priority = register(new BoolSetting(
            "Priority", "Prioritize most damaged item first", true));

    private final TimerUtil timer = new TimerUtil();
    private boolean notified = false;

    public AnvilRepair() {
        super("AnvilRepair", "Automatically repairs tools at nearby anvil", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        notified = false;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(1000)) return;
        timer.reset();

        if (!needsRepair()) return;

        if (mc.player.currentScreenHandler instanceof AnvilScreenHandler) {
            // Already at anvil — place item in first slot
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    0, 0,
                    net.minecraft.screen.slot.SlotActionType.PICKUP, mc.player);
            return;
        }

        // Find anvil
        BlockPos anvilPos = findNearbyAnvil(5.0);
        if (anvilPos == null) {
            if (!notified) {
                ChatUtil.warn("AnvilRepair: No anvil found nearby.");
                notified = true;
            }
            return;
        }
        notified = false;

        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(anvilPos), Direction.UP, anvilPos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
    }

    private boolean needsRepair() {
        double threshold = durability.get();
        int worstSlot = -1;
        double worstRatio = 1.0;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !stack.isDamageable()) continue;
            double ratio = 1.0 - (double) stack.getDamage() / stack.getMaxDamage();
            if (ratio < threshold) {
                if (!priority.isEnabled()) return true;
                if (ratio < worstRatio) {
                    worstRatio = ratio;
                    worstSlot = i;
                }
            }
        }
        return worstSlot != -1;
    }

    private BlockPos findNearbyAnvil(double range) {
        int r = (int) Math.ceil(range);
        BlockPos center = mc.player.getBlockPos();
        double rangeSq = range * range;
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > rangeSq) continue;
            var block = mc.world.getBlockState(pos).getBlock();
            if (block == Blocks.ANVIL || block == Blocks.CHIPPED_ANVIL || block == Blocks.DAMAGED_ANVIL) {
                return pos.toImmutable();
            }
        }
        return null;
    }
}
