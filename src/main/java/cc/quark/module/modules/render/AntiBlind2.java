package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * AntiBlind2 - removes blindness, darkness and pumpkin head overlay effects client-side.
 */
public class AntiBlind2 extends Module {

    private static AntiBlind2 instance;

    private final BoolSetting blindness = register(new BoolSetting(
            "Blindness", "Remove blindness effect", true));

    private final BoolSetting darkness = register(new BoolSetting(
            "Darkness", "Remove darkness effect (from sculk shriekers)", true));

    private final BoolSetting pumpkin = register(new BoolSetting(
            "Pumpkin", "Remove pumpkin head overlay (via mixin)", true));

    public AntiBlind2() {
        super("AntiBlind2", "Enhanced anti-blindness with multiple effects", Category.RENDER);
        instance = this;
    }

    public static AntiBlind2 getInstance() { return instance; }

    public static boolean noPumpkin() {
        return instance != null && instance.isEnabled() && instance.pumpkin.isEnabled();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (blindness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) {
            mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
        }
        if (darkness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.DARKNESS)) {
            mc.player.removeStatusEffect(StatusEffects.DARKNESS);
        }
        // Pumpkin overlay is handled via MixinInGameHud using noPumpkin()
    }
}
