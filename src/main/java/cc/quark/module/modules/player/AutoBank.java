package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AutoBank extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Radius to find nearby chests", 4.0, 1.0, 8.0));
    private final StringSetting bankItems = register(new StringSetting(
            "BankItems", "Comma-separated item names to deposit", "diamond"));

    private final TimerUtil timer = new TimerUtil();

    public AutoBank() {
        super("AutoBank", "Auto-deposits items to nearby chests", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler handler) {
            if (!timer.hasReached(150)) return;
            timer.reset();
            depositItems(handler);
            return;
        }

        if (!timer.hasReached(1000)) return;
        timer.reset();

        BlockPos chest = findChest();
        if (chest == null) return;

        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(chest), Direction.UP, chest, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
    }

    private void depositItems(GenericContainerScreenHandler handler) {
        Set<String> targets = getBankSet();
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            String name = stack.getName().getString().toLowerCase();
            if (targets.stream().noneMatch(name::contains)) continue;

            int guiSlot = i < 9 ? 36 + i + handler.getRows() * 9 : i + handler.getRows() * 9;
            // Shift-click to move directly
            mc.interactionManager.clickSlot(handler.syncId, guiSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
            return;
        }
        // Done — close
        mc.player.closeHandledScreen();
    }

    private BlockPos findChest() {
        int r = (int) Math.ceil(range.get());
        double rangeSq = range.get() * range.get();
        BlockPos center = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > rangeSq) continue;
            var block = mc.world.getBlockState(pos).getBlock();
            if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.BARREL) {
                return pos.toImmutable();
            }
        }
        return null;
    }

    private Set<String> getBankSet() {
        Set<String> set = new HashSet<>();
        for (String s : bankItems.get().split(",")) {
            String t = s.trim().toLowerCase();
            if (!t.isEmpty()) set.add(t);
        }
        return set;
    }
}
