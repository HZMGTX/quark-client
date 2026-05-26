package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * FrostWalk - applies dolphin's grace while in water to glide over the surface.
 */
public class FrostWalk extends Module {

    public FrostWalk() {
        super("FrostWalk", "Glide across water surface", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isTouchingWater() || mc.player.isSubmergedInWater()) return;
        mc.player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.DOLPHINS_GRACE, 20, 1, false, false));
    }
}
