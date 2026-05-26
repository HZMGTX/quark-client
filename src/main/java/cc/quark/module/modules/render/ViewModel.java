package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class ViewModel extends Module {

    private final DoubleSetting rightHandX     = register(new DoubleSetting("Right Hand X",      "Right hand X offset",     0.0, -2.0, 2.0));
    private final DoubleSetting rightHandY     = register(new DoubleSetting("Right Hand Y",      "Right hand Y offset",     0.0, -2.0, 2.0));
    private final DoubleSetting rightHandZ     = register(new DoubleSetting("Right Hand Z",      "Right hand Z offset",     0.0, -2.0, 2.0));
    private final DoubleSetting rightHandPitch = register(new DoubleSetting("Right Hand Pitch",  "Right hand pitch offset", 0.0, -180.0, 180.0));
    private final DoubleSetting rightHandYaw   = register(new DoubleSetting("Right Hand Yaw",    "Right hand yaw offset",   0.0, -180.0, 180.0));
    private final DoubleSetting rightHandRoll  = register(new DoubleSetting("Right Hand Roll",   "Right hand roll offset",  0.0, -180.0, 180.0));

    private final DoubleSetting leftHandX     = register(new DoubleSetting("Left Hand X",       "Left hand X offset",      0.0, -2.0, 2.0));
    private final DoubleSetting leftHandY     = register(new DoubleSetting("Left Hand Y",       "Left hand Y offset",      0.0, -2.0, 2.0));
    private final DoubleSetting leftHandZ     = register(new DoubleSetting("Left Hand Z",       "Left hand Z offset",      0.0, -2.0, 2.0));
    private final DoubleSetting leftHandPitch = register(new DoubleSetting("Left Hand Pitch",   "Left hand pitch offset",  0.0, -180.0, 180.0));
    private final DoubleSetting leftHandYaw   = register(new DoubleSetting("Left Hand Yaw",     "Left hand yaw offset",    0.0, -180.0, 180.0));
    private final DoubleSetting leftHandRoll  = register(new DoubleSetting("Left Hand Roll",    "Left hand roll offset",   0.0, -180.0, 180.0));

    private final BoolSetting noSwing       = register(new BoolSetting("No Swing",       "Disable the attack swing animation",         false));
    private final BoolSetting noBob         = register(new BoolSetting("No View Bob",    "Disable view bobbing while walking",          false));
    private final DoubleSetting fovMultiplier = register(new DoubleSetting("FOV Multiplier", "Multiplies the base FOV", 1.0, 0.5, 2.0));

    private boolean wasNoBob     = false;
    private double  savedFov     = 70.0;
    private boolean fovModified  = false;

    public ViewModel() {
        super("ViewModel", "Customizes first-person arm position, rotation, and animations", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            savedFov = mc.options.getFov().getValue();
        }
        applyNoBob();
        applyFovMultiplier();
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.getBobView().setValue(true);
            if (fovModified) {
                mc.options.getFov().setValue((int) Math.round(savedFov));
                fovModified = false;
            }
        }
        wasNoBob = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.options == null) return;
        applyNoBob();
        applyFovMultiplier();
    }

    private void applyNoBob() {
        if (mc.options == null) return;
        boolean wantNoBob = noBob.isEnabled();
        if (wantNoBob != wasNoBob) {
            mc.options.getBobView().setValue(!wantNoBob);
            wasNoBob = wantNoBob;
        }
    }

    private void applyFovMultiplier() {
        if (mc.options == null) return;
        double mult = fovMultiplier.get();
        if (Math.abs(mult - 1.0) > 0.001) {
            double newFov = Math.max(1, Math.min(110, savedFov * mult));
            mc.options.getFov().setValue((int) Math.round(newFov));
            fovModified = true;
        } else if (fovModified) {
            mc.options.getFov().setValue((int) Math.round(savedFov));
            fovModified = false;
        }
    }

    public double getRightHandX()    { return rightHandX.get();    }
    public double getRightHandY()    { return rightHandY.get();    }
    public double getRightHandZ()    { return rightHandZ.get();    }
    public double getRightHandPitch(){ return rightHandPitch.get();}
    public double getRightHandYaw()  { return rightHandYaw.get();  }
    public double getRightHandRoll() { return rightHandRoll.get(); }

    public double getLeftHandX()    { return leftHandX.get();    }
    public double getLeftHandY()    { return leftHandY.get();    }
    public double getLeftHandZ()    { return leftHandZ.get();    }
    public double getLeftHandPitch(){ return leftHandPitch.get();}
    public double getLeftHandYaw()  { return leftHandYaw.get();  }
    public double getLeftHandRoll() { return leftHandRoll.get(); }

    public boolean isNoSwing() { return isEnabled() && noSwing.isEnabled(); }
    public boolean isNoBob()   { return isEnabled() && noBob.isEnabled();   }
}
