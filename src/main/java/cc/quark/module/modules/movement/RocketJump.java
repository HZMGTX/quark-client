package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class RocketJump extends Module {

    private final BoolSetting useFirework = register(new BoolSetting(
            "UseFirework", "Use firework rockets to trigger the boost", true));

    private final DoubleSetting jumpBoost = register(new DoubleSetting(
            "Jump Boost", "Upward velocity applied on trigger", 1.2, 0.3, 3.0));

    public RocketJump() {
        super("RocketJump", "Uses explosion knockback to jump higher", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;

        boolean jumpPressed = mc.options.jumpKey.isPressed();
        if (!jumpPressed) return;

        if (useFirework.isEnabled() && hasFirework()) {
            // Use elytra rocket mechanic: briefly jump and fire rocket
            mc.player.jump();
            Vec3d vel = mc.player.getVelocity();
            float yaw = mc.player.getYaw();
            double yawRad = Math.toRadians(yaw);
            mc.player.setVelocity(
                    vel.x - Math.sin(yawRad) * 0.5,
                    jumpBoost.get(),
                    vel.z + Math.cos(yawRad) * 0.5
            );
            mc.player.fallDistance = 0;
            // Fire the rocket item to consume it
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        } else if (!useFirework.isEnabled()) {
            // Pure velocity boost jump without rocket
            mc.player.jump();
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, jumpBoost.get(), vel.z);
            mc.player.fallDistance = 0;
        }
    }

    private boolean hasFirework() {
        var main = mc.player.getMainHandStack();
        if (main.getItem() == Items.FIREWORK_ROCKET) return true;
        var off = mc.player.getOffHandStack();
        return off.getItem() == Items.FIREWORK_ROCKET;
    }
}
