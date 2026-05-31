package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoFortify extends Module {

    private final IntSetting hpThreshold = register(new IntSetting("HPThreshold", "Place obsidian when HP falls below this", 8, 1, 20));
    private final TimerUtil timer = new TimerUtil();

    private static final Direction[] SIDES = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    public AutoFortify() {
        super("AutoFortify", "Places obsidian around yourself when health drops below threshold", Category.COMBAT);
    }

    private int findObsidianSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem bi && bi.getBlock() == Blocks.OBSIDIAN) return i;
        }
        return -1;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.getHealth() > hpThreshold.get()) return;
        if (!timer.hasReached(200)) return;

        int slot = findObsidianSlot();
        if (slot == -1) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int prevSlot = mc.player.getInventory().selectedSlot;

        for (Direction dir : SIDES) {
            BlockPos placePos = playerPos.offset(dir);
            if (!mc.world.getBlockState(placePos).isAir()) continue;
            BlockPos supportPos = placePos.down();
            if (mc.world.getBlockState(supportPos).isAir()) continue;

            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
            mc.player.getInventory().selectedSlot = slot;

            BlockHitResult hitResult = new BlockHitResult(supportPos.toCenterPos(), Direction.UP, supportPos, false);
            ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);

            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
            mc.player.getInventory().selectedSlot = prevSlot;

            if (result.isAccepted()) {
                timer.reset();
                return;
            }
        }
    }
}
