package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

/**
 * AntiDrown - keeps the player alive underwater by:
 *  1. Refilling air bubbles to maximum every tick (optional).
 *  2. Auto-swimming upward when submerged (optional).
 *  3. Applying a Water Breathing potion effect (optional).
 */
public class AntiDrown extends Module {

    private final BoolSetting airRefill = register(new BoolSetting(
            "Air Refill", "Restore air bubbles to max each tick while submerged", true));
    private final BoolSetting autoAscend = register(new BoolSetting(
            "Auto Ascend", "Apply upward velocity while fully submerged", true));
    private final DoubleSetting ascendSpeed = register(new DoubleSetting(
            "Ascend Speed", "Upward velocity applied when auto-ascending", 0.15, 0.05, 0.5));
    private final BoolSetting waterBreathing = register(new BoolSetting(
            "Water Breathing", "Apply Water Breathing effect while in water", true));
    private final IntSetting airThreshold = register(new IntSetting(
            "Air Threshold", "Auto-ascend when air bubbles drop below this value", 150, 0, 300));

    public AntiDrown() {
        super("AntiDrown", "Prevent drowning — refills air and auto-ascends underwater", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (!mc.player.isTouchingWater()) return;

        // Apply Water Breathing status effect so vanilla air-drain stops
        if (waterBreathing.isEnabled()) {
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WATER_BREATHING, 40, 0, false, false));
        }

        boolean submerged = mc.player.isSubmergedInWater();

        if (submerged) {
            // Restore air to max
            if (airRefill.isEnabled()) {
                mc.player.setAir(mc.player.getMaxAir());
            }

            // Auto-ascend when air is running low or always if threshold is max
            int currentAir = mc.player.getAir();
            if (autoAscend.isEnabled() && currentAir <= airThreshold.get()) {
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(vel.x, Math.max(vel.y, ascendSpeed.get()), vel.z);
            }
        }
    }
}
