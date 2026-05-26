package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;

public class NoFog extends Module {

    private final BoolSetting voidFog   = register(new BoolSetting("Void Fog",   "Disable dark void fog below Y=0",           true));
    private final BoolSetting caveFog   = register(new BoolSetting("Cave Fog",   "Disable underground darkness/cave fog",      true));
    private final BoolSetting waterFog  = register(new BoolSetting("Water Fog",  "Reduce fog density while underwater",        true));
    private final BoolSetting lavaFog   = register(new BoolSetting("Lava Fog",   "Remove lava fog",                            true));
    private final BoolSetting blindFog  = register(new BoolSetting("Blind Fog",  "Cancel blindness and darkness effect fog",   true));

    public NoFog() {
        super("NoFog", "Removes various fog effects from the world", Category.RENDER, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (blindFog.isEnabled()) {
            mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
            mc.player.removeStatusEffect(StatusEffects.DARKNESS);
        }
    }

    public boolean isRemovingVoidFog()  { return isEnabled() && voidFog.isEnabled();  }
    public boolean isRemovingCaveFog()  { return isEnabled() && caveFog.isEnabled();  }
    public boolean isRemovingWaterFog() { return isEnabled() && waterFog.isEnabled(); }
    public boolean isRemovingLavaFog()  { return isEnabled() && lavaFog.isEnabled();  }
    public boolean isRemovingBlindFog() { return isEnabled() && blindFog.isEnabled(); }
}
