package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.Vec3d;

public class AntiVoid2 extends Module {

    private final BoolSetting teleport = register(new BoolSetting(
            "Teleport", "Teleport to last surface position when triggered", true));

    private Vec3d lastSurface = null;

    public AntiVoid2() {
        super("AntiVoid2", "Cancels downward motion when near void (y < 10)", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        lastSurface = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        double y = mc.player.getY();

        if (mc.player.isOnGround() && y > 10) {
            lastSurface = mc.player.getPos();
        }

        if (y < 10) {
            Vec3d vel = mc.player.getVelocity();
            if (vel.y < 0) {
                mc.player.setVelocity(vel.x, 0, vel.z);
            }
            mc.player.fallDistance = 0;

            if (teleport.isEnabled() && lastSurface != null) {
                mc.player.setPos(lastSurface.x, lastSurface.y, lastSurface.z);
                mc.player.setVelocity(0, 0, 0);
            }
        }
    }
}
