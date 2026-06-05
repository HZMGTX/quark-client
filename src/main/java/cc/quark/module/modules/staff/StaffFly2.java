package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;

public class StaffFly2 extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Fly speed multiplier", 2.0, 0.5, 20.0));
    private final DoubleSetting verticalSpeed = register(new DoubleSetting(
            "Vertical Speed", "Vertical ascent/descent speed", 1.0, 0.1, 10.0));
    private final BoolSetting noFall = register(new BoolSetting(
            "No Fall", "Cancel fall damage while flying", true));
    private final BoolSetting antiKick = register(new BoolSetting(
            "Anti Kick", "Reset fall distance each tick to avoid kick", true));
    private final BoolSetting sprintFly = register(new BoolSetting(
            "Sprint Fly", "Apply sprint multiplier when sprinting", true));

    private boolean wasFlying = false;
    private boolean hadAllowFly = false;

    public StaffFly2() {
        super("StaffFly2", "Enhanced fly mode with speed control for staff", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        mc.getEventBus().subscribe(this);

        wasFlying = mc.player.getAbilities().flying;
        hadAllowFly = mc.player.getAbilities().allowFlying;

        mc.player.getAbilities().allowFlying = true;
        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlySpeed((float) (speed.get() * 0.05f));
        mc.player.sendAbilitiesUpdate();

        ChatUtil.info("§6[StaffFly2] §fEnabled — speed §e" + speed.get() + "x");
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        if (mc.player == null) return;

        mc.player.getAbilities().allowFlying = hadAllowFly;
        mc.player.getAbilities().flying = wasFlying;
        mc.player.getAbilities().setFlySpeed(0.05f); // restore default
        mc.player.sendAbilitiesUpdate();

        ChatUtil.info("§6[StaffFly2] §fDisabled — fly restored to default.");
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Keep fly speed in sync with the setting (user may change it while flying)
        double currentSpeed = speed.get();
        if (sprintFly.isEnabled() && mc.player.isSprinting()) {
            currentSpeed *= 1.5;
        }
        mc.player.getAbilities().setFlySpeed((float) (currentSpeed * 0.05));

        // Make sure fly flags stay enabled
        mc.player.getAbilities().allowFlying = true;
        mc.player.getAbilities().flying = true;

        // Anti-kick: reset fall distance so the server doesn't detect a fall
        if (antiKick.isEnabled()) {
            mc.player.fallDistance = 0.0f;
        }

        // No-fall: clear fall damage by keeping fallDistance at 0
        if (noFall.isEnabled()) {
            mc.player.fallDistance = 0.0f;
        }

        // Vertical speed adjustment via velocity when holding jump/sneak
        boolean jumpHeld = mc.options.jumpKey.isPressed();
        boolean sneakHeld = mc.options.sneakKey.isPressed();

        if (jumpHeld) {
            mc.player.setVelocity(mc.player.getVelocity().x, verticalSpeed.get() * 0.1, mc.player.getVelocity().z);
        } else if (sneakHeld) {
            mc.player.setVelocity(mc.player.getVelocity().x, -verticalSpeed.get() * 0.1, mc.player.getVelocity().z);
        }
    }
}
