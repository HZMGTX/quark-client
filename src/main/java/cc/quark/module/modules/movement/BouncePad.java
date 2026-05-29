package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.util.math.Vec3d;

/**
 * BouncePad - launches the player upward on landing.
 *
 * <ul>
 *   <li><b>Auto</b> - bounce on every landing regardless of input.</li>
 *   <li><b>Hold</b> - only bounce when the sneak (crouch) key is held on landing.</li>
 * </ul>
 */
public class BouncePad extends Module {

    private final DoubleSetting height = register(new DoubleSetting(
            "Height", "Upward velocity applied on bounce", 2.5, 0.5, 10.0));
    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Activation mode", "Auto", "Auto", "Hold"));
    private final DoubleSetting horizontalBoost = register(new DoubleSetting(
            "Horizontal Boost", "Multiplier applied to current horizontal speed on bounce", 1.0, 0.5, 3.0));

    private boolean wasInAir = false;

    public BouncePad() {
        super("BouncePad", "Bounce upward on landing", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasInAir = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();

        // Detect the landing tick: was in air last tick, now on ground
        if (onGround && wasInAir) {
            boolean shouldBounce = mode.is("Auto")
                    || (mode.is("Hold") && mc.options.sneakKey.isPressed());

            if (shouldBounce) {
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(
                        vel.x * horizontalBoost.get(),
                        height.get(),
                        vel.z * horizontalBoost.get());
                mc.player.fallDistance = 0;
            }
        }

        wasInAir = !onGround;
    }
}
