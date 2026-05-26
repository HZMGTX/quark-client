package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import cc.quark.setting.IntSetting;

/**
 * Boost - applies a Speed status effect to the player for a flat movement boost.
 */
public class Boost extends Module {

    private final IntSetting amplifier = register(new IntSetting(
            "Amplifier", "Speed effect level", 2, 0, 10));

    public Boost() {
        super("Boost", "Applies a speed effect", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        mc.player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SPEED, 30, amplifier.get(), false, false));
    }
}
