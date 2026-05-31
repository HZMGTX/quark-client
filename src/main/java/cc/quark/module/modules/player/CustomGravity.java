package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class CustomGravity extends Module {

    private final DoubleSetting multiplier = register(new DoubleSetting(
            "Multiplier", "Gravity strength multiplier (1.0 = normal)", 0.5, 0.1, 2.0));

    public CustomGravity() {
        super("CustomGravity", "Adjusts player gravity strength", Category.PLAYER);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround() || mc.player.isSubmergedInWater() || mc.player.isSubmergedIn(net.minecraft.fluid.Fluids.LAVA)) return;

        double vel = mc.player.getVelocity().y;
        double gravity = 0.08;
        double adjusted = vel - (gravity * multiplier.get() - gravity);
        mc.player.setVelocity(mc.player.getVelocity().x, adjusted, mc.player.getVelocity().z);
    }
}
