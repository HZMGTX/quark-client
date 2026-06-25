package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.EntityUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

import java.util.List;

public class AntiWitch extends Module {

    private final TimerUtil timer = new TimerUtil();
    private boolean hitByWitch = false;

    public AntiWitch() {
        super("AntiWitch", "Auto-drinks milk when hit by a witch to remove negative effects", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        hitByWitch = false;
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (mc.player == null || mc.world == null) return;
        List<WitchEntity> witches = EntityUtil.getEntitiesOfType(WitchEntity.class, 16.0);
        if (!witches.isEmpty()) {
            hitByWitch = true;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!hitByWitch) return;
        if (!timer.hasReached(500)) return;

        int milkSlot = -1;
        int prevSlot = mc.player.getInventory().selectedSlot;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.MILK_BUCKET) {
                milkSlot = i;
                break;
            }
        }
        if (milkSlot == -1) {
            hitByWitch = false;
            return;
        }

        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(milkSlot));
        mc.player.getInventory().selectedSlot = milkSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
        mc.player.getInventory().selectedSlot = prevSlot;

        hitByWitch = false;
        timer.reset();
    }
}
