package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class FastHead extends Module {
    private final DoubleSetting multiplier = register(new DoubleSetting("Multiplier", "Look speed multiplier", 2.0, 1.1, 10.0));
    public FastHead() { super("FastHead", "Increases look/rotation speed", Category.MOVEMENT); }
    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;
        float factor = (float) multiplier.getValue();
        float dy = event.getYaw() - mc.player.prevYaw;
        float dp = event.getPitch() - mc.player.prevPitch;
        event.setYaw(mc.player.prevYaw + dy * factor);
        event.setPitch(Math.max(-90, Math.min(90, mc.player.prevPitch + dp * factor)));
    }
}
