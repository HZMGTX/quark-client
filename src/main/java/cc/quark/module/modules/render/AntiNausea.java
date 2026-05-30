package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;

public class AntiNausea extends Module {

    private final BoolSetting removeEffect = register(new BoolSetting("RemoveEffect", "Actively remove nausea status effect", true));

    public AntiNausea() {
        super("AntiNausea", "Removes the nausea effect that causes screen wobbling", Category.RENDER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!removeEffect.isEnabled()) return;
        if (mc.player.hasStatusEffect(StatusEffects.NAUSEA)) {
            mc.player.removeStatusEffect(StatusEffects.NAUSEA);
        }
    }
}
