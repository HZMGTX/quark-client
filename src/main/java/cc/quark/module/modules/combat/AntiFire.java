package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * AntiFire — applies Fire Resistance every tick and extinguishes the player.
 * Separate BoolSettings let the user toggle each behaviour.
 */
public class AntiFire extends Module {

    private final BoolSetting extinguish     = register(new BoolSetting("Extinguish",      "Extinguish burn time every tick",         true));
    private final BoolSetting fireResistance = register(new BoolSetting("Fire Resistance", "Apply Fire Resistance effect every tick",  true));

    public AntiFire() {
        super("AntiFire", "Applies fire resistance and extinguishes fire every tick", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (extinguish.isEnabled() && mc.player.isOnFire()) {
            mc.player.extinguish();
            mc.player.setFireTicks(0);
        }
        if (fireResistance.isEnabled() && !mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 400, 0, false, false));
        }
    }
}
