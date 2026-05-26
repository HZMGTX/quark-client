package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * Dolphin - applies Dolphin's Grace while swimming for faster water travel.
 */
public class Dolphin extends Module {

    public Dolphin() {
        super("Dolphin", "Faster swimming via Dolphin's Grace", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isTouchingWater()) return;
        mc.player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.DOLPHINS_GRACE, 30, 0, false, false));
    }
}
