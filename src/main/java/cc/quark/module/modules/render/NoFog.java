package cc.quark.module.modules.render;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;

public class NoFog extends Module {

    private final BoolSetting blindness = register(new BoolSetting("Blindness", "Remove blindness effect", true));
    private final BoolSetting caveFog = register(new BoolSetting("Cave Fog", "Remove cave darkness fog", true));
    private final BoolSetting lavaFog = register(new BoolSetting("Lava Fog", "Remove lava fog", true));
    private final BoolSetting waterFog = register(new BoolSetting("Water Fog", "Remove water fog", true));

    public NoFog() {
        super("NoFog", "Removes fog effects", Category.RENDER, 0);
    }

    @Override
    public void onEnable() {
        Quark.getInstance().getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        Quark.getInstance().getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (blindness.getValue()) mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
        if (blindness.getValue()) mc.player.removeStatusEffect(StatusEffects.DARKNESS);
    }

    public boolean isRemovingCaveFog() { return isEnabled() && caveFog.getValue(); }
    public boolean isRemovingLavaFog() { return isEnabled() && lavaFog.getValue(); }
    public boolean isRemovingWaterFog() { return isEnabled() && waterFog.getValue(); }
}
