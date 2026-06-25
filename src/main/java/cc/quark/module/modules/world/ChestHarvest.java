package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
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

import java.util.ArrayList;
import java.util.List;

public class ChestHarvest extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Chest search radius", 5, 1, 10));
    private final IntSetting delay = register(new IntSetting("Delay", "Delay between chest opens (ms)", 500, 100, 2000));

    private final TimerUtil timer = new TimerUtil();
    private final List<BlockPos> visited = new ArrayList<>();
    private boolean looting = false;

    public ChestHarvest() {
        super("ChestHarvest", "Loots all accessible chests in range", Category.WORLD);
    }

    @Override
    public void onEnable() {
        visited.clear();
        looting = false;
        timer.reset();
    }

    @Override
    public void onDisable() {
        visited.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (mc.currentScreen instanceof HandledScreen<?>) {
            if (!timer.hasReached(100)) return;
            var handler = mc.player.currentScreenHandler;
            int containerSize = handler.slots.size() - 36;
            if (containerSize <= 0) return;

            for (int i = 0; i < containerSize; i++) {
                if (!handler.slots.get(i).hasStack()) continue;
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                timer.reset();
                return;
            }

            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(handler.syncId));
            mc.currentScreen.close();
            looting = false;
            timer.reset();
            return;
        }

        if (looting) return;
        if (!timer.hasReached(delay.get())) return;

        BlockPos center = mc.player.getBlockPos();
        int r = range.get();
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            var block = mc.world.getBlockState(pos).getBlock();
            if (block != Blocks.CHEST && block != Blocks.TRAPPED_CHEST) continue;
            BlockPos immutable = pos.toImmutable();
            if (visited.contains(immutable)) continue;
            visited.add(immutable);
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, immutable, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            looting = true;
            timer.reset();
            return;
        }
    }
}
