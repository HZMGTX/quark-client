package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class SlowFallToggle extends Module {

    private final BoolSetting onlyFalling = register(new BoolSetting("Only Falling", "Only apply when Y velocity is negative", false));

    public SlowFallToggle() {
        super("SlowFallToggle", "Toggle slow falling effect", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.removeStatusEffect(StatusEffects.SLOW_FALLING);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (onlyFalling.isEnabled() && mc.player.getVelocity().y >= 0) {
            mc.player.removeStatusEffect(StatusEffects.SLOW_FALLING);
            return;
        }
        mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, Integer.MAX_VALUE, 0, false, false));
    }
}
