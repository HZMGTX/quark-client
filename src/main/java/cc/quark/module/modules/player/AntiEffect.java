package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;

public class AntiEffect extends Module {

    private final ModeSetting cancel = register(new ModeSetting(
            "Cancel", "Which effect to counter with milk", "All", "Poison", "Wither", "Slowness", "All"));

    private final TimerUtil timer = new TimerUtil();

    public AntiEffect() {
        super("AntiEffect", "Cancels specific negative potion effects by drinking milk", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!timer.hasReached(2000)) return;
        timer.reset();

        boolean shouldDrink = false;
        String mode = cancel.get();

        switch (mode) {
            case "Poison"   -> shouldDrink = mc.player.hasStatusEffect(StatusEffects.POISON);
            case "Wither"   -> shouldDrink = mc.player.hasStatusEffect(StatusEffects.WITHER);
            case "Slowness" -> shouldDrink = mc.player.hasStatusEffect(StatusEffects.SLOWNESS);
            case "All"      -> shouldDrink = mc.player.hasStatusEffect(StatusEffects.POISON)
                    || mc.player.hasStatusEffect(StatusEffects.WITHER)
                    || mc.player.hasStatusEffect(StatusEffects.SLOWNESS)
                    || mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                    || mc.player.hasStatusEffect(StatusEffects.NAUSEA);
        }

        if (!shouldDrink) return;

        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.MILK_BUCKET) {
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
}
