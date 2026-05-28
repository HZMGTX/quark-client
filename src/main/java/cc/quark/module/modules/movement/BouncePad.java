package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.util.math.Vec3d;

public class BouncePad extends Module {

    private final DoubleSetting height = register(new DoubleSetting("Height", "Upward velocity on bounce", 2.5, 1.0, 5.0));
    private final ModeSetting mode = register(new ModeSetting("Mode", "Activation mode", "Hold", "Hold", "Toggle"));

    private boolean toggled = false;
    private boolean wasOnGround = false;

    public BouncePad() {
        super("BouncePad", "Bounces player upward on key press", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        toggled = false;
        wasOnGround = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();
        boolean jumpPressed = mc.options.jumpKey.isPressed();

        if (mode.is("Toggle")) {
            if (jumpPressed && !wasOnGround && onGround) {
                toggled = !toggled;
            }
            if (toggled && onGround) {
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(vel.x, height.get(), vel.z);
            }
        } else {
            if (jumpPressed && onGround) {
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(vel.x, height.get(), vel.z);
            }
        }

        wasOnGround = onGround;
    }
}
