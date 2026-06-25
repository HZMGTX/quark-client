package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class PotionHelper extends Module {

    private final BoolSetting strengthPot = register(new BoolSetting("Strength Pot", "Auto-drink strength potions", true));
    private final BoolSetting speedPot = register(new BoolSetting("Speed Pot", "Auto-drink speed potions", true));
    private final BoolSetting regenPot = register(new BoolSetting("Regen Pot", "Auto-drink regeneration potions", false));
    private final IntSetting hpThreshold = register(new IntSetting("HP Threshold", "Health below which regen pot is used", 15, 1, 20));

    private final TimerUtil timer = new TimerUtil();

    public PotionHelper() {
        super("PotionHelper", "Auto-drinks combat potions from inventory", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(1000)) return;
        if (mc.player.isUsingItem()) return;

        float hp = mc.player.getHealth();

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() != Items.POTION) continue;

            PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (contents == null) continue;

            boolean shouldDrink = false;

            for (var effect : contents.getEffects()) {
                var effectType = effect.getEffectType().value();
                if (strengthPot.isEnabled() && effectType == StatusEffects.STRENGTH.value()
                        && !mc.player.hasStatusEffect(StatusEffects.STRENGTH)) {
                    shouldDrink = true;
                }
                if (speedPot.isEnabled() && effectType == StatusEffects.SPEED.value()
                        && !mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                    shouldDrink = true;
                }
                if (regenPot.isEnabled() && effectType == StatusEffects.REGENERATION.value()
                        && hp <= hpThreshold.get()
                        && !mc.player.hasStatusEffect(StatusEffects.REGENERATION)) {
                    shouldDrink = true;
                }
            }

            if (shouldDrink) {
                mc.player.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                timer.reset();
                return;
            }
        }
    }
}
