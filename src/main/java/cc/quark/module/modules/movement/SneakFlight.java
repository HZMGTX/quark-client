package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class SneakFlight extends Module {
    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Flight speed", 0.3, 0.1, 2.0));
    public SneakFlight() { super("SneakFlight", "Hold sneak to fly downward, jump to fly up", Category.MOVEMENT); }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.options.sneakKey.isPressed() && !mc.player.isOnGround()) {
            mc.player.setVelocity(mc.player.getVelocity().x, -speed.getValue(), mc.player.getVelocity().z);
        }
    }
}
