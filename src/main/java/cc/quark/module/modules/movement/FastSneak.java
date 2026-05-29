package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * FastSneak - applies a Speed status effect while the player is sneaking so
 * movement in crouch mode is no longer painfully slow.
 *
 * <p>The effect level is configurable: level 1 = Speed I, level 2 = Speed II, etc.
 */
public class FastSneak extends Module {

    private final IntSetting level = register(new IntSetting(
            "Level", "Speed effect amplifier (1 = Speed I, 2 = Speed II …)", 2, 1, 5));

    public FastSneak() {
        super("FastSneak", "Speed effect while sneaking to counter the slow-down", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isSneaking()) return;

        // Apply the speed effect for a short duration so it stays active continuously
        mc.player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SPEED,
                10,                      // duration: refresh every tick
                level.get() - 1,         // amplifier (0-based)
                false,                   // is ambient
                false));                 // show particles
    }
}
