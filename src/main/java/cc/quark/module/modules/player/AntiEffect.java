package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;

public class AntiEffect extends Module {

    private final BoolSetting blindness = register(new BoolSetting(
            "Blindness", "Cancel blindness effect", true));

    private final BoolSetting nausea = register(new BoolSetting(
            "Nausea", "Cancel nausea effect", true));

    private final BoolSetting slowness = register(new BoolSetting(
            "Slowness", "Cancel slowness effect", false));

    public AntiEffect() {
        super("AntiEffect", "Cancels specific negative potion effects", Category.PLAYER);
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
        if (slowness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            mc.player.removeStatusEffect(StatusEffects.SLOWNESS);
        }
    }
}
