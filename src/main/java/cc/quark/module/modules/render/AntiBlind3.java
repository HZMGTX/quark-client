package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;

public class AntiBlind3 extends Module {

    private final BoolSetting blindness = register(new BoolSetting(
            "Blindness", "Remove blindness effect", true));

    private final BoolSetting nausea = register(new BoolSetting(
            "Nausea", "Remove nausea effect", true));

    private final BoolSetting darkness = register(new BoolSetting(
            "Darkness", "Remove darkness effect", true));

    public AntiBlind3() {
        super("AntiBlind3", "Removes all screen overlay effects", Category.RENDER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (blindness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) {
            mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
        }
        if (nausea.isEnabled() && mc.player.hasStatusEffect(StatusEffects.NAUSEA)) {
            mc.player.removeStatusEffect(StatusEffects.NAUSEA);
        }
        if (darkness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.DARKNESS)) {
            mc.player.removeStatusEffect(StatusEffects.DARKNESS);
        }
    }
}
