package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class CreativeFly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Flight speed", 0.05, 0.01, 1.0));

    private boolean wasFlying = false;
    private boolean wasAllowFlying = false;
    private float prevFlySpeed = 0.05f;

    public CreativeFly() {
        super("CreativeFly", "Creative-mode flight in survival via abilities", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        wasAllowFlying = mc.player.getAbilities().allowFlying;
        wasFlying = mc.player.getAbilities().flying;
        prevFlySpeed = mc.player.getAbilities().getFlySpeed();
        mc.player.getAbilities().allowFlying = true;
        mc.player.getAbilities().flying = true;
        mc.player.sendAbilitiesUpdate();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.getAbilities().allowFlying = wasAllowFlying;
        mc.player.getAbilities().flying = wasFlying;
        mc.player.getAbilities().setFlySpeed(prevFlySpeed);
        mc.player.sendAbilitiesUpdate();
        mc.player.setVelocity(0, 0, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        mc.player.getAbilities().allowFlying = true;
        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlySpeed((float) speed.get());
        mc.player.fallDistance = 0;
    }
}
