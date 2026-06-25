package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.Vec3d;

/**
 * LiquidWalk - walk on top of any liquid (water and/or lava) without sinking.
 *
 * <p>Each tick, if the player is touching the chosen liquid and is not
 * submerged, any downward velocity is cancelled to keep them at the surface.
 * Sneak to dive below the surface.
 */
public class LiquidWalk extends Module {

    private final BoolSetting water = register(new BoolSetting(
            "Water", "Walk on water", true));

    private final BoolSetting lava = register(new BoolSetting(
            "Lava", "Walk on lava", false));

    public LiquidWalk() {
        super("LiquidWalk", "Walk on top of any liquid", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isSneaking()) return;

        boolean touchingWater = mc.player.isTouchingWater();
        boolean touchingLava  = mc.player.isInLava();

        boolean onWater = water.isEnabled() && touchingWater && !mc.player.isSubmergedInWater();
        boolean onLava  = lava.isEnabled()  && touchingLava;

        if (!onWater && !onLava) return;

        Vec3d vel = mc.player.getVelocity();
        if (vel.y < 0) {
            mc.player.setVelocity(vel.x, 0.0, vel.z);
        }
        mc.player.setOnGround(true);
    }
}
