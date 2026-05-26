package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * SlowFallToggle - applies slow falling whenever the player is descending.
 */
public class SlowFallToggle extends Module {

    public SlowFallToggle() {
        super("SlowFallToggle", "Slow falling on descent", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround() || mc.player.getVelocity().y >= 0) return;
        mc.player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOW_FALLING, 20, 0, false, false));
    }
}
