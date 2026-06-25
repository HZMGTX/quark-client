package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class AutoPotion2 extends Module {

    private final DoubleSetting hpThreshold = register(new DoubleSetting(
            "HpThreshold", "Throw health potion when HP is at or below this value", 10.0, 1.0, 20.0));

    private final BoolSetting strengthPot = register(new BoolSetting(
            "StrengthPot", "Also use strength potions offensively", false));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Delay between potion uses in milliseconds", 2000, 200, 10000));

    private final TimerUtil timer = new TimerUtil();

    public AutoPotion2() {
        super("AutoPotion2", "Throws splash potions at self or enemies", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        float hp = mc.player.getHealth();
        if (hp > (float) hpThreshold.get() && !strengthPot.isEnabled()) return;

        // Find a splash potion in hotbar or inventory
        int slot = findSplashPotion();
        if (slot == -1) return;

        var inv = mc.player.getInventory();

        if (slot >= 9) {
            // Move from inventory to hotbar slot 7
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    slot, 7, SlotActionType.SWAP, mc.player);
            slot = 7;
        }

        int prevSlot = inv.selectedSlot;
        inv.selectedSlot = slot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        inv.selectedSlot = prevSlot;

        timer.reset();
    }

    private int findSplashPotion() {
        var inv = mc.player.getInventory();

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (isSplashHealthPotion(stack)) return i;
        }
        for (int i = 9; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (isSplashHealthPotion(stack)) return i;
        }
        return -1;
    }

    private boolean isSplashHealthPotion(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() != Items.SPLASH_POTION) return false;

        //? if mc >= "1.20.5" {
        var potionContents = stack.get(net.minecraft.component.DataComponentTypes.POTION_CONTENTS);
        if (potionContents == null) return false;
        return potionContents.potion()
                .map(p -> p.value().getEffects().stream().anyMatch(e ->
                        e.getEffectType().value().isBeneficial()))
                .orElse(false);
        //?} else {
        /*net.minecraft.potion.PotionUtil.getPotion(stack);
        return net.minecraft.potion.PotionUtil.getPotionEffects(stack).stream()
                .anyMatch(e -> e.getEffectType().isBeneficial());*/
        //?}
    }
}
