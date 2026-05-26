package cc.quark.module.modules.render;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPostMotion;
import cc.quark.event.events.EventPreMotion;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class FreeLook extends Module {

    private final BoolSetting lockBody   = register(new BoolSetting("Lock Body",    "Lock body yaw to enable direction while looking freely", true));
    private final DoubleSetting sensitivity = register(new DoubleSetting("Sensitivity", "Camera sensitivity multiplier", 1.0, 0.1, 3.0));

    private float lockedYaw;
    private float lockedPitch;

    private float cameraYaw;
    private float cameraPitch;

    private double prevDeltaX = 0;
    private double prevDeltaY = 0;

    public FreeLook() {
        super("FreeLook", "Look around freely without rotating body or sending rotation to server", Category.RENDER, 0);
    }

    @Override
    public void onEnable() {
        Quark.getInstance().getEventBus().subscribe(this);
        if (mc.player != null) {
            lockedYaw   = mc.player.getYaw();
            lockedPitch = mc.player.getPitch();
            cameraYaw   = lockedYaw;
            cameraPitch = lockedPitch;
        }
        prevDeltaX = 0;
        prevDeltaY = 0;
    }

    @Override
    public void onDisable() {
        Quark.getInstance().getEventBus().unsubscribe(this);
        if (mc.player != null) {
            mc.player.setYaw(cameraYaw);
            mc.player.setPitch(cameraPitch);
            mc.player.prevYaw   = cameraYaw;
            mc.player.prevPitch = cameraPitch;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.mouse == null) return;

        double rawDX = mc.mouse.getCursorDeltaX();
        double rawDY = mc.mouse.getCursorDeltaY();

        double deltaDX = rawDX - prevDeltaX;
        double deltaDY = rawDY - prevDeltaY;
        prevDeltaX = rawDX;
        prevDeltaY = rawDY;

        double sens = mc.options.getMouseSensitivity().getValue() * sensitivity.get();
        float yawDelta   = (float)(deltaDX * sens * 0.15);
        float pitchDelta = (float)(deltaDY * sens * 0.15);

        cameraYaw   += yawDelta;
        cameraPitch += pitchDelta;
        cameraPitch  = Math.max(-90f, Math.min(90f, cameraPitch));

        mc.player.setYaw(cameraYaw);
        mc.player.setPitch(cameraPitch);
        mc.player.prevYaw   = cameraYaw;
        mc.player.prevPitch = cameraPitch;
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;
        if (lockBody.isEnabled()) {
            event.setYaw(lockedYaw);
            event.setPitch(lockedPitch);
        }
    }

    @EventHandler
    public void onPostMotion(EventPostMotion event) {
        if (mc.player == null) return;
        mc.player.setYaw(cameraYaw);
        mc.player.setPitch(cameraPitch);
        mc.player.prevYaw   = cameraYaw;
        mc.player.prevPitch = cameraPitch;
    }

    public float getSavedYaw()   { return lockedYaw;   }
    public float getSavedPitch() { return lockedPitch; }
}
