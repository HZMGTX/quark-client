package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventJump;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class JumpBoost extends Module {
    private final DoubleSetting boost = register(new DoubleSetting("Boost", "Extra jump velocity", 0.2, 0.0, 2.0));
    public JumpBoost() { super("JumpBoost", "Boosts jump height with extra velocity", Category.MOVEMENT); }
    @EventHandler
    public void onJump(EventJump event) {
        if (mc.player == null) return;
        mc.player.addVelocity(0, boost.getValue(), 0);
    }
}
