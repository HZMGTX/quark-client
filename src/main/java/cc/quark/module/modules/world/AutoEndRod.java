package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoEndRod extends Module {

    private final BoolSetting autoEquip = register(new BoolSetting(
            "AutoEquip", "Switch to end rods automatically", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoEndRod() {
        super("AutoEndRod", "Places end rods as lighting when walking through dark areas", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(800)) return;

        BlockPos feet = mc.player.getBlockPos();
        int lightLevel = mc.world.getLightLevel(feet);
        if (lightLevel >= 8) return;

        BlockPos below = feet.down();
        if (!mc.world.getBlockState(below).isSolidBlock(mc.world, below)) return;
        if (!mc.world.getBlockState(feet).isAir()) return;

        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.END_ROD) {
                slot = i;
                break;
            }
        }
        if (slot == -1) return;

        int saved = mc.player.getInventory().selectedSlot;
        if (autoEquip.isEnabled()) {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
            mc.player.getInventory().selectedSlot = slot;
        } else if (mc.player.getInventory().getStack(mc.player.getInventory().selectedSlot).getItem() != Items.END_ROD) {
            return;
        }

        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(below).add(0, 0.5, 0), Direction.UP, below, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        timer.reset();

        if (autoEquip.isEnabled()) {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(saved));
            mc.player.getInventory().selectedSlot = saved;
        }
    }
}
