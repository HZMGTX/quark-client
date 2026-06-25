package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.util.math.Vec3d;

public class HorseSpeed extends Module {

    private final DoubleSetting multiplier = register(new DoubleSetting("Multiplier", "Speed multiplier for horse", 2.0, 1.0, 10.0));
    private final BoolSetting autoJump = register(new BoolSetting("AutoJump", "Automatically jump while riding", false));

    private int jumpCooldown = 0;

    public HorseSpeed() {
        super("HorseSpeed", "Increases rideable horse speed", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        jumpCooldown = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!(mc.player.getVehicle() instanceof AbstractHorseEntity horse)) return;

        Vec3d vel = horse.getVelocity();
        double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (hSpeed > 0.01) {
            double factor = multiplier.get();
            horse.setVelocity(vel.x * factor, vel.y, vel.z * factor);
            horse.velocityModified = true;
        }

        if (autoJump.isEnabled() && horse.isOnGround()) {
            if (jumpCooldown <= 0) {
                horse.setVelocity(horse.getVelocity().x, 0.5, horse.getVelocity().z);
                horse.velocityModified = true;
                jumpCooldown = 10;
            } else {
                jumpCooldown--;
            }
        }
    }
}
