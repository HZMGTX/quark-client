package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class ClimbSpeed extends Module {
    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Climb speed multiplier", 2.0, 1.0, 8.0));
    public ClimbSpeed() { super("ClimbSpeed", "Increases climbing speed on ladders/vines", Category.MOVEMENT); }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || !mc.player.isClimbing()) return;
        mc.player.setVelocity(mc.player.getVelocity().x,
            mc.player.getVelocity().y * speed.getValue(), mc.player.getVelocity().z);
    }
}
