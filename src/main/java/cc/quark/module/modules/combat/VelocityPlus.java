package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;

public class VelocityPlus extends Module {
    private final ModeSetting  mode   = register(new ModeSetting ("Mode","Velocity cancel method","Cancel","Cancel","Reduce","Reverse"));
    private final DoubleSetting factor = register(new DoubleSetting("Factor","Velocity reduction factor",0.0,0.0,1.0));

    public VelocityPlus() { super("VelocityPlus","Advanced knockback velocity manipulation",Category.COMBAT); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player==null) return;
        var vel = mc.player.getVelocity();
        if (Math.abs(vel.x)<0.01&&Math.abs(vel.z)<0.01) return;
        switch(mode.get()) {
            case "Cancel"  -> mc.player.setVelocity(0, vel.y, 0);
            case "Reduce"  -> mc.player.setVelocity(vel.x*factor.get(), vel.y, vel.z*factor.get());
            case "Reverse" -> mc.player.setVelocity(-vel.x, vel.y, -vel.z);
        }
    }
}
