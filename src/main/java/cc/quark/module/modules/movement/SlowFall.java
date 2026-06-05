package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class SlowFall extends Module {
    private final DoubleSetting fallSpeed = register(new DoubleSetting("Fall Speed", "Max fall speed", -0.1, -1.0, 0.0));

    public SlowFall() { super("SlowFall", "Makes you fall slowly like a feather", Category.MOVEMENT); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.player.isOnGround()) return;
        var vel = mc.player.getVelocity();
        if (vel.y < fallSpeed.get()) mc.player.setVelocity(vel.x, fallSpeed.get(), vel.z);
    }
}
