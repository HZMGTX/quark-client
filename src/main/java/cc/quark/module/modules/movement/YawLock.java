package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class YawLock extends Module {

    private final BoolSetting onlySprinting = register(new BoolSetting(
            "OnlySprinting", "Only lock yaw when the player is sprinting", true));

    private float lockedYaw = 0f;
    private boolean locked = false;

    public YawLock() {
        super("YawLock", "Locks yaw rotation during movement for straight-line speed",
                Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        locked = false;
    }

    @Override
    public void onDisable() {
        locked = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;
        boolean sprinting = mc.player.isSprinting();

        boolean shouldLock = moving && (!onlySprinting.isEnabled() || sprinting);

        if (shouldLock && !locked) {
            lockedYaw = mc.player.getYaw();
            locked = true;
        } else if (!shouldLock) {
            locked = false;
        }
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (!locked) return;
        event.setYaw(lockedYaw);
    }
}
