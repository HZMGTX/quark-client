package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoBarrel extends Module {

    private final BoolSetting depositAll = register(new BoolSetting("DepositAll", "Deposit all items into barrels", true));
    private final IntSetting range = register(new IntSetting("Range", "Search radius for barrels", 4, 1, 8));
    private final IntSetting delay = register(new IntSetting("Delay", "Delay between slot transfers (ms)", 100, 50, 500));

    private final TimerUtil timer = new TimerUtil();
    private boolean depositing = false;

    public AutoBarrel() {
        super("AutoBarrel", "Auto opens and transfers items to nearby barrels", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
        depositing = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (mc.currentScreen instanceof HandledScreen<?>) {
            if (!timer.hasReached(delay.get())) return;
            var handler = mc.player.currentScreenHandler;
            int containerSize = handler.slots.size() - 36;
            if (containerSize <= 0) return;

            int playerStart = containerSize;
            int playerEnd = handler.slots.size() - 9;
            int hotbarEnd = handler.slots.size();

            int start = depositAll.isEnabled() ? playerStart : playerStart;
            int end = depositAll.isEnabled() ? hotbarEnd : playerEnd;

            for (int i = start; i < end; i++) {
                if (!handler.slots.get(i).hasStack()) continue;
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                timer.reset();
                return;
            }

            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(handler.syncId));
            mc.currentScreen.close();
            depositing = false;
            return;
        }

        depositing = false;
        if (!timer.hasReached(500)) return;

        BlockPos center = mc.player.getBlockPos();
        int r = range.get();
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            if (!mc.world.getBlockState(pos).isOf(Blocks.BARREL)) continue;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            timer.reset();
            depositing = true;
            return;
        }
    }
}
