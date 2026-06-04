package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoBarrel extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to detect barrels", 3.0, 1.0, 6.0));
    private final BoolSetting autoSort = register(new BoolSetting(
            "AutoSort", "Auto-sort barrel contents when opened", true));

    private final TimerUtil timer = new TimerUtil();
    private boolean acted = false;

    public AutoBarrel() {
        super("AutoBarrel", "Auto-manages barrel contents (sort/collect)", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
        acted = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(600)) return;

        // If a barrel is already open, perform sort/collect action
        if (mc.player.currentScreenHandler != null
                && !(mc.player.currentScreenHandler == mc.player.playerScreenHandler)) {
            if (autoSort.isEnabled() && !acted) {
                sortOpenContainer();
                acted = true;
                timer.reset();
                return;
            }
            acted = false;
            timer.reset();
            return;
        }

        acted = false;
        int r = (int) Math.ceil(range.get());
        double rangeSq = range.get() * range.get();
        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > rangeSq) continue;
            if (!mc.world.getBlockState(pos).isOf(Blocks.BARREL)) continue;
            var be = mc.world.getBlockEntity(pos);
            if (!(be instanceof BarrelBlockEntity)) continue;

            Vec3d hitVec = Vec3d.ofCenter(pos).add(0, 0.5, 0);
            BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            timer.reset();
            return;
        }
        timer.reset();
    }

    private void sortOpenContainer() {
        if (mc.player == null || mc.interactionManager == null) return;
        var handler = mc.player.currentScreenHandler;
        int size = handler.slots.size();
        int containerEnd = size - 36; // player inventory starts after container slots

        // Quick-move all items into player inventory then back to sort them
        for (int i = 0; i < containerEnd; i++) {
            var slot = handler.slots.get(i);
            if (slot.hasStack()) {
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            }
        }
        // Move player inventory items back into barrel
        for (int i = containerEnd; i < size; i++) {
            var slot = handler.slots.get(i);
            if (slot.hasStack()) {
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            }
        }
    }
}
