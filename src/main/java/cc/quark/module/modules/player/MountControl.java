package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.vehicle.BoatEntity;

public class MountControl extends Module {
    private final BoolSetting horseBoost = register(new BoolSetting("Horse Boost", "Boost horse speed", true));
    private final BoolSetting boatBoost = register(new BoolSetting("Boat Boost", "Boost boat speed", true));
    private final DoubleSetting speedMult = register(new DoubleSetting("Speed Mult", "Speed multiplier", 1.5, 1.0, 5.0));

    public MountControl() { super("MountControl", "Enhanced control of mounts and vehicles", Category.PLAYER); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        var vehicle = mc.player.getVehicle();
        if (horseBoost.isEnabled() && vehicle instanceof HorseEntity horse) {
            var vel = horse.getVelocity();
            horse.setVelocity(vel.x * speedMult.get(), vel.y, vel.z * speedMult.get());
        } else if (boatBoost.isEnabled() && vehicle instanceof BoatEntity boat) {
            var vel = boat.getVelocity();
            boat.setVelocity(vel.x * speedMult.get(), vel.y, vel.z * speedMult.get());
        }
    }
}
