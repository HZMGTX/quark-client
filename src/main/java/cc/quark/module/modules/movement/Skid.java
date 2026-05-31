package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.Vec3d;

public class Skid extends Module {

    private final BoolSetting onlySprinting = register(new BoolSetting(
            "OnlySprinting", "Only stop momentum when sprinting", false));

    public Skid() {
        super("Skid", "Instantly stops momentum on release of movement keys", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;

        boolean moving = mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
        if (moving) return;

        if (onlySprinting.isEnabled() && !mc.player.isSprinting()) return;

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(0, vel.y, 0);
        mc.player.setSprinting(false);
    }
}
