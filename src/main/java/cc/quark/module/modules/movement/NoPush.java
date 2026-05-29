package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.Vec3d;

/**
 * NoPush - detect sudden horizontal velocity spikes not caused by player input
 * (entity pushes, piston borders) and zero them out.
 */
public class NoPush extends Module {

    private final BoolSetting entities = register(new BoolSetting(
            "Entities", "Cancel pushes from entity collisions", true));
    private final BoolSetting blocks = register(new BoolSetting(
            "Blocks", "Cancel block-border (piston) pushes", true));

    private double prevX = 0;
    private double prevZ = 0;

    public NoPush() {
        super("NoPush", "Zero unexpected velocity spikes from entity/piston pushes", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        prevX = 0;
        prevZ = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean hasInput = mc.player.input.movementForward != 0
                        || mc.player.input.movementSideways != 0;

        Vec3d vel = mc.player.getVelocity();

        if (!hasInput) {
            // No input: any horizontal velocity must come from external push
            if (entities.isEnabled() || blocks.isEnabled()) {
                mc.player.setVelocity(0.0, vel.y, 0.0);
            }
        } else {
            // With input: detect spike > threshold compared to last tick
            double dx = vel.x - prevX;
            double dz = vel.z - prevZ;
            double spike = Math.sqrt(dx * dx + dz * dz);
            if (spike > 0.3) {
                // Absorb the spike component
                if (entities.isEnabled()) {
                    mc.player.setVelocity(prevX, vel.y, prevZ);
                }
            }
        }

        prevX = mc.player.getVelocity().x;
        prevZ = mc.player.getVelocity().z;
    }
}
