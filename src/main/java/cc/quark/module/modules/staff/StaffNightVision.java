package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class StaffNightVision extends Module {
    public StaffNightVision() {
        super("Staff NV", "Permanent night vision for staff use", Category.STAFF, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        var existing = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (existing == null || existing.getDuration() < 40) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 800, 0, false, false));
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
    }
}
