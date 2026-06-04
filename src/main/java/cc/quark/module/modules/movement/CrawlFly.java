package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * CrawlFly - allows the player to fly while in the crawling (swimming) pose,
 * reducing their hitbox height and helping them navigate tight spaces while
 * airborne.
 *
 * <p>When {@code Auto Toggle} is enabled the crawl (swim) state is forced on
 * while the module is active and the player is not on the ground, giving a
 * smaller collision profile in flight.
 */
public class CrawlFly extends Module {

    private final BoolSetting autoToggle = register(new BoolSetting(
            "Auto Toggle", "Automatically enter crawl pose while flying", true));

    private final DoubleSetting flySpeed = register(new DoubleSetting(
            "Fly Speed", "Horizontal fly speed (blocks/tick)", 0.3, 0.05, 3.0));

    private final DoubleSetting vertSpeed = register(new DoubleSetting(
            "Vert Speed", "Vertical fly speed (blocks/tick)", 0.2, 0.05, 2.0));

    public CrawlFly() {
        super("CrawlFly", "Crawl while flying to reduce collision", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        // Force swim (crawl) pose to shrink hitbox while airborne
        if (autoToggle.isEnabled()) {
            mc.player.setSwimming(true);
        }

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        boolean jumpHeld  = mc.options.jumpKey.isPressed();
        boolean sneakHeld = mc.options.sneakKey.isPressed();

        double spd    = flySpeed.get();
        double yawRad = Math.toRadians(mc.player.getYaw());

        double dx = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * spd;
        double dz = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * spd;

        double dy = 0;
        if (jumpHeld)  dy =  vertSpeed.get();
        if (sneakHeld) dy = -vertSpeed.get();

        Vec3d vel = mc.player.getVelocity();
        if (fwd != 0 || side != 0) {
            mc.player.setVelocity(dx, dy != 0 ? dy : vel.y * 0.8, dz);
        } else if (dy != 0) {
            mc.player.setVelocity(vel.x * 0.8, dy, vel.z * 0.8);
        } else {
            mc.player.setVelocity(vel.x * 0.8, vel.y * 0.8, vel.z * 0.8);
        }
        mc.player.fallDistance = 0;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setSwimming(false);
        }
    }
}
