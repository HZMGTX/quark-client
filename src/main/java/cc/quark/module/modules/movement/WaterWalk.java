package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * WaterWalk - walk on water by zeroing downward velocity when touching water
 * and not submerged. Sneak to dive below the surface.
 */
public class WaterWalk extends Module {

    private final BoolSetting spoof = register(new BoolSetting(
            "Spoof Ground", "Also spoof onGround=true above water surface", false));

    public WaterWalk() {
        super("WaterWalk", "Walk on water surface; Sneak to sink; optional onGround spoof", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isTouchingWater()) return;
        if (mc.player.isSneaking()) return;
        if (mc.player.isSubmergedInWater()) return;

        double vy = mc.player.getVelocity().y;
        if (vy < 0) {
            mc.player.setVelocity(mc.player.getVelocity().x, 0.0, mc.player.getVelocity().z);
        }

        if (spoof.isEnabled()) {
            mc.player.setOnGround(true);
        }
    }
}
