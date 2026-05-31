package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class InventoryFill extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Range to search for nearby storage", 4, 1, 8));

    private final TimerUtil timer = new TimerUtil();

    public InventoryFill() {
        super("InventoryFill", "Fills empty inventory slots from nearby storage containers", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(1000)) return;
        timer.reset();

        boolean hasEmpty = false;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) {
                hasEmpty = true;
                break;
            }
        }
        if (!hasEmpty) return;

        BlockPos center = mc.player.getBlockPos();
        int r = range.get();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            BlockEntity be = mc.world.getBlockEntity(pos);
            if (!(be instanceof ChestBlockEntity)) continue;

            if (mc.player.squaredDistanceTo(Vec3d.ofCenter(pos)) > r * r) continue;

            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            return;
        }
    }
}
