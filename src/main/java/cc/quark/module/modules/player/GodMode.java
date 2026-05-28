package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class GodMode extends Module {

    private final BoolSetting cancelDamage = register(new BoolSetting("Cancel Damage", "Cancel incoming damage events", true));

    public GodMode() {
        super("GodMode", "Applies client-side resistance and regeneration", Category.PLAYER);
    }

    @Override
    public String getSuffix() {
        return "ACTIVE";
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 4, false, false));
        mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 1, false, false));
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (cancelDamage.isEnabled()) {
            event.cancel();
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.removeStatusEffect(StatusEffects.RESISTANCE);
        mc.player.removeStatusEffect(StatusEffects.REGENERATION);
    }
}
