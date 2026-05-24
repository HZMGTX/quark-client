package com.ghostclient.module.modules.movement;

import com.ghostclient.GhostClient;
import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.DoubleSetting;

public class FastFall extends Module {

    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Fall speed multiplier", 3.0, 1.0, 10.0));

    public FastFall() {
        super("FastFall", "Fall faster when sneaking", Category.MOVEMENT, 0);
    }

    @Override
    public void onEnable() {
        GhostClient.getInstance().getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        GhostClient.getInstance().getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround() && mc.options.sneakKey.isPressed()) {
            mc.player.setVelocity(mc.player.getVelocity().x, -speed.getValue() * 0.1, mc.player.getVelocity().z);
        }
    }
}
