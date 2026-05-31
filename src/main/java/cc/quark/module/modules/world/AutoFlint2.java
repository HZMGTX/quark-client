package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoFlint2 extends Module {

    private final BoolSetting autoUse = register(new BoolSetting(
            "AutoUse", "Auto-use flint and steel immediately after acquiring it", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoFlint2() {
        super("AutoFlint2", "Crafts flint and steel when inventory has required materials", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(500)) return;
        timer.reset();

        boolean hasFlintSteel = false;
        int flintSteelSlot = -1;
        boolean hasFlint = false;
        boolean hasIronIngot = false;

        for (int i = 0; i < 36; i++) {
            var item = mc.player.getInventory().getStack(i).getItem();
            if (item == Items.FLINT_AND_STEEL) {
                hasFlintSteel = true;
                if (i < 9) flintSteelSlot = i;
            }
            if (item == Items.FLINT) hasFlint = true;
            if (item == Items.IRON_INGOT) hasIronIngot = true;
        }

        if (!hasFlintSteel && hasFlint && hasIronIngot) {
            if (mc.currentScreen == null) {
                mc.player.openHandledScreen(null);
            }
            return;
        }

        if (autoUse.isEnabled() && flintSteelSlot != -1) {
            BlockPos lookAt = mc.player.getBlockPos().offset(mc.player.getHorizontalFacing());
            if (mc.world.getBlockState(lookAt).isSolidBlock(mc.world, lookAt)) {
                int saved = mc.player.getInventory().selectedSlot;
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(flintSteelSlot));
                mc.player.getInventory().selectedSlot = flintSteelSlot;
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(lookAt), Direction.UP, lookAt, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(saved));
                mc.player.getInventory().selectedSlot = saved;
            }
        }
    }
}
