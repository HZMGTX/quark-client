package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * MoonWalk - low gravity jumping via jump boost while airborne.
 */
public class MoonWalk extends Module {

    public MoonWalk() {
        super("MoonWalk", "Low gravity jump boost", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        mc.player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.JUMP_BOOST, 20, 1, false, false));
        mc.player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOW_FALLING, 20, 0, false, false));
    }
}
