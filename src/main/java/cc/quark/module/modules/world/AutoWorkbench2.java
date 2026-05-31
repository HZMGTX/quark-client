package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoWorkbench2 extends Module {

    private final ModeSetting recipe = register(new ModeSetting("Recipe", "Which recipe to auto-craft", "Planks",
            "Planks", "Sticks", "Chest", "Crafting Table", "Furnace"));

    private final TimerUtil timer = new TimerUtil();

    public AutoWorkbench2() {
        super("AutoWorkbench2", "Opens workbench and crafts a configured item repeatedly", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (mc.currentScreen instanceof CraftingScreen) {
            if (!timer.hasReached(200)) return;
            var handler = mc.player.currentScreenHandler;
            // slot 0 is output; quick-move if it has a result
            if (handler.slots.get(0).hasStack()) {
                mc.interactionManager.clickSlot(handler.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
                timer.reset();
                return;
            }
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(handler.syncId));
            mc.currentScreen.close();
            return;
        }

        if (mc.currentScreen instanceof HandledScreen<?>) return;

        if (!timer.hasReached(600)) return;

        BlockPos center = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-3, -1, -3), center.add(3, 1, 3))) {
            if (!mc.world.getBlockState(pos).isOf(Blocks.CRAFTING_TABLE)) continue;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            timer.reset();
            return;
        }
    }
}
