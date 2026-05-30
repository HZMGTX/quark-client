package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;

/**
 * NoJumpAnimation - suppresses the leg-kicking jump animation by briefly
 * spoofing onGround=true via EventPreMotion. The actual velocity boost from
 * jumping is still applied; only the animation packet is affected.
 */
public class NoJumpAnimation extends Module {

    private boolean wasOnGround = true;

    public NoJumpAnimation() {
        super("NoJumpAnimation", "Suppresses the jump animation by spoofing onGround in motion packets", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasOnGround = mc.player != null && mc.player.isOnGround();
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();
        double velY = mc.player.getVelocity().y;

        // Detect the frame of a jump: just left the ground with upward velocity
        if (wasOnGround && !onGround && velY > 0.1) {
            // Spoof onGround=true so the server sees a smooth transition
            // without the leg-kick animation packet
            event.setOnGround(true);
        }

        wasOnGround = onGround;
    }
}
