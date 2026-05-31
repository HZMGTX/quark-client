package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class AutoElytra2 extends Module {

    private final IntSetting altitudeMin = register(new IntSetting(
            "AltitudeMin", "Minimum Y altitude before auto-boosting", 80, 20, 320));

    private final TimerUtil boostTimer = new TimerUtil();

    public AutoElytra2() {
        super("AutoElytra2", "Manages elytra flight — auto-equips, boosts, lands", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        ItemStack chest = mc.player.getEquippedStack(EquipmentSlot.CHEST);

        if (!(chest.getItem() instanceof ElytraItem)) {
            equipElytra();
            return;
        }

        if (mc.player.isFallFlying()) {
            if (mc.player.getY() < altitudeMin.get() && boostTimer.hasReached(3000)) {
                boostTimer.reset();
                useFirework();
            }
        }
    }

    private void equipElytra() {
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof ElytraItem) {
                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        i, 6, SlotActionType.SWAP, mc.player);
                return;
            }
        }
    }

    private void useFirework() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.FIREWORK_ROCKET) {
                int prev = mc.player.getInventory().selectedSlot;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(i));
                mc.player.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prev));
                mc.player.getInventory().selectedSlot = prev;
                return;
            }
        }
    }
}
