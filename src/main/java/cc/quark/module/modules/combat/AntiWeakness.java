package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.InventoryUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Hand;

/**
 * AntiWeakness — while the Weakness effect is active:
 * - removes the effect client-side every tick so the HUD looks normal, AND
 * - on EventAttack: silently switches to the best sword, attacks, then switches back.
 */
public class AntiWeakness extends Module {

    private final BoolSetting removeEffect = register(new BoolSetting("Remove Effect", "Remove Weakness effect client-side every tick", true));
    private final BoolSetting autoSwitch   = register(new BoolSetting("Auto Switch",   "Switch to best sword while Weakness is active",  true));

    public AntiWeakness() {
        super("AntiWeakness", "Bypasses Weakness effect on attack", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (removeEffect.isEnabled() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
            mc.player.removeStatusEffect(StatusEffects.WEAKNESS);
        }
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!autoSwitch.isEnabled()) return;
        if (!mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) return;

        int bestSword = InventoryUtil.findBestSword();
        if (bestSword == -1) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = bestSword;

        mc.interactionManager.attackEntity(mc.player, event.getTarget());
        mc.player.swingHand(Hand.MAIN_HAND);

        mc.player.getInventory().selectedSlot = prevSlot;
    }
}
