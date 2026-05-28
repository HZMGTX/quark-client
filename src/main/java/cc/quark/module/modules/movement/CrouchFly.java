package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class CrouchFly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Downward glide speed when sneaking airborne", 0.1, 0.05, 0.5));

    public CrouchFly() {
        super("CrouchFly", "Hold sneak while airborne to glide downward slowly instead of free-falling", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isSneaking()) return;
        if (mc.player.isOnGround()) return;

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x, -speed.get(), vel.z);
        mc.player.fallDistance = 0;
    }
}
