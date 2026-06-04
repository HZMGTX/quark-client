package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AntiHeal extends Module {

    private final BoolSetting cancelRegen = register(new BoolSetting("Cancel Regen", "Cancel regeneration effect", true));
    private final BoolSetting cancelPotions = register(new BoolSetting("Cancel Potions", "Prevent drinking healing potions", false));
    private final BoolSetting onlyInCombat = register(new BoolSetting("Only In Combat", "Only apply effects when in combat", false));

    public AntiHeal() {
        super("AntiHeal", "Prevents healing during combat for realism/testing", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (cancelRegen.isEnabled()) {
            if (mc.player.hasStatusEffect(StatusEffects.REGENERATION)) {
                mc.player.removeStatusEffect(StatusEffects.REGENERATION);
            }
        }

        if (cancelPotions.isEnabled()) {
            var stack = mc.player.getActiveItem();
            if (!stack.isEmpty() && stack.getItem() == Items.POTION) {
                mc.player.clearActiveItem();
            }
        }
    }
}
