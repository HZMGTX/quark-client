package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.client.gui.DrawContext;

public class ViewModel extends Module {

    private final DoubleSetting rightHandX   = register(new DoubleSetting("Right Hand X",     "Right hand X offset",    0.0, -2.0, 2.0));
    private final DoubleSetting rightHandY   = register(new DoubleSetting("Right Hand Y",     "Right hand Y offset",    0.0, -2.0, 2.0));
    private final DoubleSetting rightHandZ   = register(new DoubleSetting("Right Hand Z",     "Right hand Z offset",    0.0, -2.0, 2.0));
    private final DoubleSetting rightHandPitch = register(new DoubleSetting("Right Hand Pitch","Right hand pitch offset",0.0, -180.0, 180.0));
    private final DoubleSetting rightHandYaw   = register(new DoubleSetting("Right Hand Yaw",  "Right hand yaw offset",  0.0, -180.0, 180.0));
    private final DoubleSetting rightHandRoll  = register(new DoubleSetting("Right Hand Roll", "Right hand roll offset", 0.0, -180.0, 180.0));

    private final DoubleSetting leftHandX   = register(new DoubleSetting("Left Hand X",     "Left hand X offset",    0.0, -2.0, 2.0));
    private final DoubleSetting leftHandY   = register(new DoubleSetting("Left Hand Y",     "Left hand Y offset",    0.0, -2.0, 2.0));
    private final DoubleSetting leftHandZ   = register(new DoubleSetting("Left Hand Z",     "Left hand Z offset",    0.0, -2.0, 2.0));
    private final DoubleSetting leftHandPitch = register(new DoubleSetting("Left Hand Pitch","Left hand pitch offset",0.0, -180.0, 180.0));
    private final DoubleSetting leftHandYaw   = register(new DoubleSetting("Left Hand Yaw",  "Left hand yaw offset",  0.0, -180.0, 180.0));
    private final DoubleSetting leftHandRoll  = register(new DoubleSetting("Left Hand Roll", "Left hand roll offset", 0.0, -180.0, 180.0));

    private final BoolSetting noSwing  = register(new BoolSetting("No Swing", "Disable the attack swing animation", false));
    private final BoolSetting noBob    = register(new BoolSetting("No Bob",   "Disable view bob while walking",      false));

    private boolean wasNoBob = false;

    public ViewModel() {
        super("ViewModel", "Customizes the first-person arm position, rotation, and animations", Category.RENDER);
    }

    @Override
    public void onEnable() {
        applyNoBob();
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.getBobView().setValue(true);
        }
        wasNoBob = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.options == null) return;
        applyNoBob();
    }

    private void applyNoBob() {
        if (mc.options == null) return;
        boolean wantNoBob = noBob.isEnabled();
        if (wantNoBob != wasNoBob) {
            mc.options.getBobView().setValue(!wantNoBob);
            wasNoBob = wantNoBob;
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
