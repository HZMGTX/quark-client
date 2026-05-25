package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.Vec3d;

public class NoPush extends Module {

    private final BoolSetting entities = register(new BoolSetting(
            "Entities", "Don't get pushed by entities", true));
    private final BoolSetting blocks = register(new BoolSetting(
            "Blocks", "Don't get pushed by piston/block borders", true));

    public NoPush() {
        super("NoPush", "Prevents other entities and block mechanics from pushing your player", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;

        if (!moving && entities.isEnabled()) {
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(0.0, vel.y, 0.0);
        }
    }
}
