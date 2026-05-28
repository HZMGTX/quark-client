package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class FreeLook extends Module {

    private final BoolSetting lockBody = register(new BoolSetting(
            "Lock Body", "Lock body/packet yaw so server sees a fixed rotation while you look freely", true));

    private float lockedYaw;
    private float lockedPitch;

    public FreeLook() {
        super("FreeLook", "Look around freely without rotating your body or sending real rotation to server", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            lockedYaw   = mc.player.getYaw();
            lockedPitch = mc.player.getPitch();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!lockBody.isEnabled()) {
            lockedYaw   = mc.player.getYaw();
            lockedPitch = mc.player.getPitch();
        }
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;
        if (lockBody.isEnabled()) {
            event.setYaw(lockedYaw);
            event.setPitch(lockedPitch);
        }
    }

    public float getSavedYaw()   { return lockedYaw;   }
    public float getSavedPitch() { return lockedPitch; }
}
