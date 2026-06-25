package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class PistonFly extends Module {
    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Flight speed", 0.8, 0.1, 3.0));
    private final BoolSetting glide = register(new BoolSetting("Glide", "Slow glide when not pressing keys", true));
    public PistonFly() { super("PistonFly", "Fly using piston-abuse technique", Category.MOVEMENT); }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        mc.player.setVelocity(mc.player.getVelocity().multiply(1, glide.getValue() ? 0.98 : 0, 1));
    }
}
