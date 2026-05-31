package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.CartographyTableScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoCartography extends Module {

    private final IntSetting delay = register(new IntSetting("Delay", "Delay between copy actions (ms)", 300, 100, 2000));

    private final TimerUtil timer = new TimerUtil();

    public AutoCartography() {
        super("AutoCartography", "Auto copies maps on cartography table", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (mc.currentScreen instanceof CartographyTableScreen) {
            if (!timer.hasReached(delay.get())) return;
            var handler = mc.player.currentScreenHandler;

            boolean hasMap = handler.slots.get(0).hasStack();
            boolean hasPaper = handler.slots.get(1).hasStack();

            if (!hasMap) {
                for (int i = 2; i < handler.slots.size(); i++) {
                    if (handler.slots.get(i).hasStack() && handler.slots.get(i).getStack().isOf(Items.FILLED_MAP)) {
                        mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                        timer.reset();
                        return;
                    }
                }
            }

            if (!hasPaper) {
                for (int i = 2; i < handler.slots.size(); i++) {
                    if (handler.slots.get(i).hasStack() && handler.slots.get(i).getStack().isOf(Items.PAPER)) {
                        mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                        timer.reset();
                        return;
                    }
                }
            }

            if (hasMap && hasPaper && handler.slots.get(2).hasStack()) {
                mc.interactionManager.clickSlot(handler.syncId, 2, 0, SlotActionType.QUICK_MOVE, mc.player);
                timer.reset();
            }
            return;
        }

        if (!(mc.currentScreen instanceof HandledScreen<?>)) {
            if (!timer.hasReached(500)) return;
            BlockPos center = mc.player.getBlockPos();
            for (BlockPos pos : BlockPos.iterate(center.add(-3, -1, -3), center.add(3, 1, 3))) {
                if (!mc.world.getBlockState(pos).isOf(Blocks.CARTOGRAPHY_TABLE)) continue;
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.toImmutable(), false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                timer.reset();
                return;
            }
        }
    }
}
