package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class AutoLavaWalk extends Module {

    private final BoolSetting autoDrink = register(new BoolSetting(
            "AutoDrink", "Automatically drink the fire resistance potion when lava is detected ahead", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoLavaWalk() {
        super("AutoLavaWalk", "Drinks fire-resistance potion when walking towards lava", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!autoDrink.isEnabled()) return;
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(1000)) return;
        timer.reset();

        if (mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) return;

        if (!lavaAhead()) return;

        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.POTION) {
                var effects = net.minecraft.item.PotionContentsComponent.getEffects(stack);
                boolean hasFireRes = false;
                for (var eff : effects) {
                    if (eff.getEffectType().equals(StatusEffects.FIRE_RESISTANCE)) {
                        hasFireRes = true;
                        break;
                    }
                }
                if (!hasFireRes) continue;

                int prev = mc.player.getInventory().selectedSlot;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(i));
                mc.player.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prev));
                mc.player.getInventory().selectedSlot = prev;
                break;
            }
        }
    }

    private boolean lavaAhead() {
        if (mc.player == null || mc.world == null) return false;
        double yaw = Math.toRadians(mc.player.getYaw());
        int px = (int) mc.player.getX();
        int py = (int) mc.player.getY();
        int pz = (int) mc.player.getZ();
        for (int d = 1; d <= 3; d++) {
            int bx = px + (int) Math.round(-Math.sin(yaw) * d);
            int bz = pz + (int) Math.round(Math.cos(yaw) * d);
            BlockPos pos = new BlockPos(bx, py, bz);
            if (mc.world.getBlockState(pos).getBlock() == Blocks.LAVA
                    || mc.world.getBlockState(pos.down()).getBlock() == Blocks.LAVA) {
                return true;
            }
        }
        return false;
    }
}
