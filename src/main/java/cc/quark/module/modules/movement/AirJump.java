package cc.quark.module.modules.movement;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventJump;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;

public class AirJump extends Module {

    private final IntSetting count = register(new IntSetting("Extra Jumps", "Number of extra jumps in air", 1, 1, 3));
    private int jumpsLeft;

    public AirJump() {
        super("AirJump", "Jump multiple times in the air", Category.MOVEMENT, 0);
    }

    @Override
    public void onEnable() {
        Quark.getInstance().getEventBus().subscribe(this);
        jumpsLeft = count.getValue();
    }

    @Override
    public void onDisable() {
        Quark.getInstance().getEventBus().unsubscribe(this);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) jumpsLeft = count.getValue();
    }

    @EventHandler
    public void onJump(EventJump event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround() && jumpsLeft > 0) {
            mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
            mc.player.fallDistance = 0;
            jumpsLeft--;
        }
    }
}
