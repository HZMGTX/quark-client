package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * ClearWater - Makes water fully transparent/clear by removing underwater fog
 * and applying a high-level Water Breathing effect to keep vision unobstructed.
 *
 * The fog suppression itself is handled via a static accessor read by the
 * game's fog-rendering path (same approach as NoFog).  The Water Breathing
 * effect ensures the overlay darkening from low oxygen is also removed.
 */
public class ClearWater extends Module {

    private static ClearWater instance;

    private final BoolSetting removeOverlay = register(new BoolSetting(
            "Remove Overlay", "Suppress the blue water-screen tint overlay", true));

    private final BoolSetting waterBreathing = register(new BoolSetting(
            "Water Breathing", "Apply Water Breathing to maintain clear vision", true));

    public ClearWater() {
        super("ClearWater", "Makes water fully transparent and removes underwater fog", Category.RENDER);
        instance = this;
    }

    public static ClearWater getInstance() { return instance; }

    /** Used by fog/overlay mixins to decide whether to skip water fog. */
    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    public static boolean shouldRemoveOverlay() {
        return isActive() && instance.removeOverlay.isEnabled();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.removeStatusEffect(StatusEffects.WATER_BREATHING);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        if (!waterBreathing.isEnabled()) return;

        StatusEffectInstance current = mc.player.getStatusEffect(StatusEffects.WATER_BREATHING);
        if (current == null || current.getDuration() < 200) {
            mc.player.addStatusEffect(
                    new StatusEffectInstance(StatusEffects.WATER_BREATHING, 9999, 0, false, false, false));
        }
    }
}
