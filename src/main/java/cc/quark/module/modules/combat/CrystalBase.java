package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public class CrystalBase extends Module {

    private final BoolSetting autoRemove = register(new BoolSetting("AutoRemove", "Auto-remove crystals that lack obsidian base", false));
    private final TimerUtil timer = new TimerUtil();

    public CrystalBase() {
        super("CrystalBase", "Places obsidian under end crystals for base protection", Category.COMBAT);
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
        if (!timer.hasReached(100)) return;

        List<EndCrystalEntity> crystals = mc.world.getEntitiesByClass(
                EndCrystalEntity.class, mc.player.getBoundingBox().expand(8), e -> true);

        for (EndCrystalEntity crystal : crystals) {
            BlockPos crystalPos = crystal.getBlockPos();
            BlockPos belowPos = crystalPos.down();
            var belowState = mc.world.getBlockState(belowPos);

            if (belowState.getBlock() != Blocks.OBSIDIAN && belowState.getBlock() != Blocks.BEDROCK) {
                if (autoRemove.isEnabled()) {
                    mc.interactionManager.attackEntity(mc.player, crystal);
                    timer.reset();
                    return;
                }

                int slot = findObsidianSlot();
                if (slot == -1) continue;

                int prevSlot = mc.player.getInventory().selectedSlot;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                mc.player.getInventory().selectedSlot = slot;

                BlockPos supportPos = belowPos.down();
                if (!mc.world.getBlockState(supportPos).isAir()) {
                    BlockHitResult hit = new BlockHitResult(belowPos.toCenterPos(), Direction.UP, supportPos, false);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                }

                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
                mc.player.getInventory().selectedSlot = prevSlot;
                timer.reset();
                return;
            }
        }
    }
}
