package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * GodMode - client-side resistance/regeneration to fake invulnerability.
 */
public class GodMode extends Module {

    public GodMode() {
        super("GodMode", "Applies client-side resistance and regeneration", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 4, false, false));
        mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 2, false, false));
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.removeStatusEffect(StatusEffects.RESISTANCE);
        mc.player.removeStatusEffect(StatusEffects.REGENERATION);
    }
}
