package com.ghostclient.module.modules.movement;

import com.ghostclient.GhostClient;
import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventJump;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.IntSetting;

public class AirJump extends Module {

    private final IntSetting count = register(new IntSetting("Extra Jumps", "Number of extra jumps in air", 1, 1, 3));
    private int jumpsLeft;

    public AirJump() {
        super("AirJump", "Jump multiple times in the air", Category.MOVEMENT, 0);
    }

    @Override
    public void onEnable() {
        GhostClient.getInstance().getEventBus().subscribe(this);
        jumpsLeft = count.getValue();
    }

    @Override
    public void onDisable() {
        GhostClient.getInstance().getEventBus().unsubscribe(this);
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
