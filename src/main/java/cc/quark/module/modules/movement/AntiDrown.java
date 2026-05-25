package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.Vec3d;

public class AntiDrown extends Module {

    private final BoolSetting airRefill = register(new BoolSetting(
            "Air Refill", "Constantly refill the air-bubble meter", true));
    private final BoolSetting autoAscend = register(new BoolSetting(
            "Auto Ascend", "Swim upward automatically when fully submerged", true));

    public AntiDrown() {
        super("AntiDrown", "Prevents drowning by managing air supply", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (mc.player.isSubmergedInWater()) {
            if (airRefill.isEnabled()) {
                mc.player.setAir(mc.player.getMaxAir());
            }
            if (autoAscend.isEnabled()) {
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(vel.x, Math.max(vel.y, 0.12), vel.z);
            }
        }
    }
}
