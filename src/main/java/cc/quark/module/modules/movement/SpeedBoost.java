package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * SpeedBoost - locally applies a Speed status effect and continuously refreshes
 * it so the player maintains it at the configured amplifier level.
 */
public class SpeedBoost extends Module {

    private final IntSetting level = register(new IntSetting(
            "Level", "Speed effect level (1 = Speed I, 5 = Speed V)", 2, 1, 5));
    private final BoolSetting hideEffect = register(new BoolSetting(
            "Hide Effect", "Hide the effect particles", false));

    public SpeedBoost() {
        super("SpeedBoost", "Apply and maintain a Speed effect locally", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.SPEED);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        StatusEffectInstance current = mc.player.getStatusEffect(StatusEffects.SPEED);
        // Re-apply when missing or about to expire (< 40 ticks remaining)
        if (current == null || current.getDuration() < 40) {
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED,
                    200,                          // 10 seconds — refreshed each tick
                    level.get() - 1,              // amplifier (0 = Speed I)
                    false,                        // not ambient
                    !hideEffect.isEnabled(),      // show particles unless hidden
                    true                          // show icon
            ));
        }
    }
}
