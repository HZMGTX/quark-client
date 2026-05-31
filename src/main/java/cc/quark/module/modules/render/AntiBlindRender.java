package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;

public class AntiBlindRender extends Module {

    public AntiBlind() {
        super("AntiBlindRender", "Removes blindness and darkness status effects visually each tick", Category.RENDER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        removeEffect(StatusEffects.BLINDNESS);
        removeEffect(StatusEffects.DARKNESS);
    }

    private void removeEffect(RegistryEntry<StatusEffect> effect) {
        StatusEffectInstance instance = mc.player.getStatusEffect(effect);
        if (instance != null) {
            mc.player.removeStatusEffect(effect);
        }
    }
}
