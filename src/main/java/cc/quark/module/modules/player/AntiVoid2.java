package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AntiVoid2 extends Module {

    private final DoubleSetting voidThreshold = register(new DoubleSetting(
            "VoidThreshold", "Y level below which to trigger void protection", -60.0, -128.0, 64.0));

    private Vec3d lastSafePos = null;

    public AntiVoid2() {
        super("AntiVoid2", "Saves last safe position and teleports back when falling into the void", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        lastSafePos = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // Save last safe position when on solid ground
        if (mc.player.isOnGround() && mc.player.getY() > voidThreshold.get()) {
            lastSafePos = mc.player.getPos();
        }

        // Trigger void protection
        if (mc.player.getY() <= voidThreshold.get()) {
            Vec3d vel = mc.player.getVelocity();

            if (lastSafePos != null) {
                // Teleport back to last safe position
                mc.player.setPos(lastSafePos.x, lastSafePos.y, lastSafePos.z);
                mc.player.setVelocity(0, 0, 0);
            } else {
                // Zero downward velocity as fallback
                mc.player.setVelocity(vel.x, 0.0, vel.z);
            }
        }
    }
}
