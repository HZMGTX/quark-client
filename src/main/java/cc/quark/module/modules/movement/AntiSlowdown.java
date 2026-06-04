package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;

public class AntiSlowdown extends Module {
    private final BoolSetting mining  = register(new BoolSetting("Mining", "Cancel mining slowdown",true));
    private final BoolSetting slowness= register(new BoolSetting("Slowness","Remove slowness effect",true));
    public AntiSlowdown() { super("AntiSlowdown","Cancels all sources of movement slowdown",Category.MOVEMENT); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player==null) return;
        if (slowness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.SLOWNESS))
            mc.player.removeStatusEffect(StatusEffects.SLOWNESS);
    }
}
