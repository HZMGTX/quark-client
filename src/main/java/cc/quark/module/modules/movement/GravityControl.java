package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;

public class GravityControl extends Module {
    private final ModeSetting mode = register(new ModeSetting("Mode", "Gravity mode", "Reduced", "Reduced", "Reversed", "Zero"));
    private final DoubleSetting factor = register(new DoubleSetting("Factor", "Gravity multiplier", 0.5, 0.0, 1.0));

    public GravityControl() { super("GravityControl", "Modifies player gravity", Category.MOVEMENT); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); if (mc.player != null) mc.player.noGravity = false; }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        switch (mode.get()) {
            case "Zero" -> mc.player.noGravity = true;
            case "Reversed" -> { mc.player.noGravity = false; var v = mc.player.getVelocity(); mc.player.setVelocity(v.x, -v.y + 0.08, v.z); }
            case "Reduced" -> { mc.player.noGravity = false; var v = mc.player.getVelocity(); if (v.y < 0) mc.player.setVelocity(v.x, v.y * factor.get(), v.z); }
        }
    }
}
