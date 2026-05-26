package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * FastSneak - applies a speed effect while crouching.
 */
public class FastSneak extends Module {

    public FastSneak() {
        super("FastSneak", "Speed effect while sneaking", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isSneaking()) return;
        mc.player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SPEED, 20, 1, false, false));
    }
}
