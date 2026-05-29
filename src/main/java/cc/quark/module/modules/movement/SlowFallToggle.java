package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * SlowFallToggle - apply Slow Falling status effect while enabled.
 * "Only Falling" setting only applies the effect when Y velocity is negative,
 * removing it while the player is rising or on ground.
 */
public class SlowFallToggle extends Module {

    private final BoolSetting onlyFalling = register(new BoolSetting(
            "Only Falling", "Only apply effect when falling (Y vel < 0)", false));

    public SlowFallToggle() {
        super("SlowFallToggle", "Apply Slow Falling; Only Falling option restricts to descent", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.removeStatusEffect(StatusEffects.SLOW_FALLING);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean isFalling = mc.player.getVelocity().y < 0;

        if (onlyFalling.isEnabled() && !isFalling) {
            mc.player.removeStatusEffect(StatusEffects.SLOW_FALLING);
            return;
        }

        // Refresh the effect every tick to keep it active indefinitely
        if (!mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOW_FALLING, Integer.MAX_VALUE, 0, false, false));
        }
    }
}
