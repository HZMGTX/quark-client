package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

/**
 * CreativeFly - enables creative-mode flight in survival by manipulating the
 * player's abilities flags.  Fly speed is configurable, and the original state
 * is fully restored when the module is disabled.
 */
public class CreativeFly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Flight speed multiplier (vanilla default = 0.05)", 0.1, 0.01, 1.0));
    private final BoolSetting noFallDamage = register(new BoolSetting(
            "No Fall Damage", "Reset fall distance each tick while flying", true));

    private boolean savedAllowFly  = false;
    private boolean savedFlying    = false;
    private float   savedFlySpeed  = 0.05f;

    public CreativeFly() {
        super("CreativeFly", "Creative-mode flight in survival via abilities flags", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        savedAllowFly = mc.player.getAbilities().allowFlying;
        savedFlying   = mc.player.getAbilities().flying;
        savedFlySpeed = mc.player.getAbilities().getFlySpeed();

        mc.player.getAbilities().allowFlying = true;
        mc.player.getAbilities().flying      = true;
        mc.player.getAbilities().setFlySpeed((float) speed.get());
        mc.player.sendAbilitiesUpdate();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.getAbilities().allowFlying = savedAllowFly;
        mc.player.getAbilities().flying      = savedFlying;
        mc.player.getAbilities().setFlySpeed(savedFlySpeed);
        mc.player.sendAbilitiesUpdate();
        // Stop floating in place when disabled
        mc.player.setVelocity(0, 0, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Keep abilities active even if the server resets them
        mc.player.getAbilities().allowFlying = true;
        mc.player.getAbilities().flying      = true;
        mc.player.getAbilities().setFlySpeed((float) speed.get());

        if (noFallDamage.isEnabled()) {
            mc.player.fallDistance = 0;
        }
    }
}
