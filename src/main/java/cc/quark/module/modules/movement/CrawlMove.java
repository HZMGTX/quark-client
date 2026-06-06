package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.pose.EntityPose;
import net.minecraft.util.math.Vec3d;

/**
 * CrawlMove - allows the player to move through 1-block-high gaps by forcing
 * the crawl (swimming on ground) pose and adjusting movement speed accordingly.
 *
 * The module forces EntityPose.SWIMMING when on the ground to allow passage
 * through 1-block gaps, and applies a speed scaling factor so crawling
 * isn't slower than expected.
 */
public class CrawlMove extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Movement speed multiplier while crawling", 1.0, 0.1, 2.0));

    private final BoolSetting autoDetect = register(new BoolSetting(
            "Auto Detect", "Only force crawl when under a low ceiling", true));

    public CrawlMove() {
        super("CrawlMove", "Move through 1-block-high gaps via crawl pose", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;

        boolean shouldCrawl = !autoDetect.isEnabled() || hasLowCeiling();
        if (!shouldCrawl) return;

        // Force the swimming/crawl pose so the player fits through 1-block gaps
        mc.player.setPose(EntityPose.SWIMMING);

        // Scale movement to configured speed
        Vec3d vel = mc.player.getVelocity();
        double hLen = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (hLen > 0.001) {
            double target = 0.12 * speed.get(); // typical crawl speed
            double scale = target / hLen;
            if (scale < 1.0) {
                mc.player.setVelocity(vel.x * scale, vel.y, vel.z * scale);
            }
        }
    }

    /** Returns true when the block directly above the player's head is solid. */
    private boolean hasLowCeiling() {
        var headPos = mc.player.getBlockPos().up(1);
        var state = mc.world.getBlockState(headPos);
        return !state.isAir();
    }
}
