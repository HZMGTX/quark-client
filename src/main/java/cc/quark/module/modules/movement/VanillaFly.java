package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * VanillaFly - toggle the player's creative-flight ability on enable/disable.
 * While enabled, supports configurable rise/fall speed. Saves the original
 * flying state and restores it on disable.
 */
public class VanillaFly extends Module {

    private final DoubleSetting flySpeed = register(new DoubleSetting(
            "Speed", "Horizontal fly speed multiplier", 1.0, 0.1, 5.0));
    private final DoubleSetting vertSpeed = register(new DoubleSetting(
            "Vertical Speed", "Rise/fall speed when Jump/Sneak held", 0.08, 0.01, 0.5));

    private boolean wasFlying = false;

    public VanillaFly() {
        super("VanillaFly", "Toggle creative flight; saves and restores flying state on toggle", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        wasFlying = mc.player.getAbilities().flying;
        mc.player.getAbilities().allowFlying = true;
        mc.player.getAbilities().flying      = true;
        mc.player.sendAbilitiesUpdate();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.getAbilities().flying = wasFlying;
        if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            mc.player.getAbilities().allowFlying = false;
        }
        mc.player.sendAbilitiesUpdate();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.getAbilities().flying) return;

        Vec3d vel = mc.player.getVelocity();
        double vy;

        if (mc.options.jumpKey.isPressed()) {
            vy = vertSpeed.get();
        } else if (mc.options.sneakKey.isPressed()) {
            vy = -vertSpeed.get();
        } else {
            vy = 0.0;
        }

        // Scale horizontal velocity by flySpeed
        mc.player.setVelocity(vel.x * flySpeed.get(), vy, vel.z * flySpeed.get());
        mc.player.fallDistance = 0;
    }
}
