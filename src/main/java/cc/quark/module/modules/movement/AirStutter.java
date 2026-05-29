package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

/**
 * AirStutter - spoof the Y position in outgoing motion packets by alternating
 * a small offset each tick: +offset one tick, -offset the next.  This creates
 * a micro-stutter that can desync server-side prediction without visible
 * movement on the client.
 */
public class AirStutter extends Module {

    private final DoubleSetting offset = register(new DoubleSetting(
            "Offset", "Y position spoof magnitude (blocks)", 0.01, 0.001, 0.1));

    private boolean flip = false;

    public AirStutter() {
        super("AirStutter", "Alternate Y+offset / Y-offset position spoof each tick", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        flip = false;
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) {
            flip = false;
            return;
        }

        double spoof = flip ? offset.get() : -offset.get();
        event.setY(event.getY() + spoof);
        flip = !flip;
    }
}
