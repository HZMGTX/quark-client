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
 * SpeedToggle - apply Speed II status effect while enabled; getSuffix shows
 * the current amplifier level. Optional ground-only setting.
 */
public class SpeedToggle extends Module {

    private final IntSetting level = register(new IntSetting(
            "Level", "Speed effect level (1 = Speed I, 2 = Speed II, etc.)", 2, 1, 5));
    private final BoolSetting groundOnly = register(new BoolSetting(
            "Ground Only", "Only apply while on ground", false));

    public SpeedToggle() {
        super("SpeedToggle", "Apply Speed effect at configurable level", Category.MOVEMENT);
    }

    @Override
    public String getSuffix() {
        return "Speed " + toRoman(level.get());
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.removeStatusEffect(StatusEffects.SPEED);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (groundOnly.isEnabled() && !mc.player.isOnGround()) return;

        mc.player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SPEED, 40, level.get() - 1, false, false));
    }

    private static String toRoman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(n);
        };
    }
}
