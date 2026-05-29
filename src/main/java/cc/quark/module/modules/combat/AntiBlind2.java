package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;

/**
 * AntiBlind2 — cancels Blindness and Darkness effects every tick.
 * Individual BoolSettings control which effects are suppressed.
 */
public class AntiBlind2 extends Module {

    private final BoolSetting blindness = register(new BoolSetting("Blindness", "Remove Blindness effect",  true));
    private final BoolSetting darkness  = register(new BoolSetting("Darkness",  "Remove Darkness effect",   true));
    private final BoolSetting nausea    = register(new BoolSetting("Nausea",    "Remove Nausea effect",     false));

    public AntiBlind2() {
        super("AntiBlind2", "Cancels blindness and darkness effects every tick", Category.COMBAT);
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
        if (nausea.isEnabled() && mc.player.hasStatusEffect(StatusEffects.NAUSEA)) {
            mc.player.removeStatusEffect(StatusEffects.NAUSEA);
        }
    }
}
