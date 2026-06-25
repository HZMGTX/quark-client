package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventDamage;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public class CombatAssist extends Module {

    private final ModeSetting mode = register(new ModeSetting("Mode", "When to switch items", "Reactive", "Reactive", "Always"));

    public CombatAssist() {
        super("CombatAssist", "Auto-uses shield when attacked and sword when attacking", Category.COMBAT);
    }

    private int findHotbarItem(Class<?> itemClass) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (itemClass.isInstance(mc.player.getInventory().getStack(i).getItem())) return i;
        }
        return -1;
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (mc.player == null) return;
        int shieldSlot = findHotbarItem(ShieldItem.class);
        if (shieldSlot == -1) return;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(shieldSlot));
        mc.player.getInventory().selectedSlot = shieldSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        if (mc.player.getMainHandStack().getItem() instanceof SwordItem) return;
        int swordSlot = findHotbarItem(SwordItem.class);
        if (swordSlot == -1) return;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(swordSlot));
        mc.player.getInventory().selectedSlot = swordSlot;
    }
}
