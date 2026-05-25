package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.ChatUtil;

public class Anchor extends Module {

    private double anchorX, anchorY, anchorZ;

    public Anchor() {
        super("Anchor", "Locks your position in place — prevents knockback from enemies or explosions", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            anchorX = mc.player.getX();
            anchorY = mc.player.getY();
            anchorZ = mc.player.getZ();
            ChatUtil.info("Anchored at " + (int) anchorX + " " + (int) anchorY + " " + (int) anchorZ);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        mc.player.setPosition(anchorX, anchorY, anchorZ);
        mc.player.setVelocity(0, 0, 0);
        mc.player.fallDistance = 0;
    }
}
