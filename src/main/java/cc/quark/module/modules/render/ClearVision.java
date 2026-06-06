package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;

public class ClearVision extends Module {
    private final BoolSetting noBlindness = register(new BoolSetting("NoBlindness", "Remove blindness effect", true));
    private final BoolSetting noNightVision = register(new BoolSetting("NightVision", "Apply night vision", true));
    private final BoolSetting noFire = register(new BoolSetting("NoFire", "Hide fire overlay", true));
    public ClearVision() { super("ClearVision", "Clears visual obstructions for maximum visibility", Category.RENDER); }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (noBlindness.getValue()) mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
    }
}
