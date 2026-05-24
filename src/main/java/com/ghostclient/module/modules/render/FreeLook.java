package com.ghostclient.module.modules.render;

import com.ghostclient.GhostClient;
import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.DoubleSetting;
import com.ghostclient.setting.ModeSetting;

public class FreeLook extends Module {

    private final ModeSetting mode = register(new ModeSetting("Mode", "FreeLook mode", "Vanilla", "Vanilla", "Lock"));
    private final DoubleSetting sensitivity = register(new DoubleSetting("Sensitivity", "Camera sensitivity", 1.0, 0.1, 3.0));

    private float savedYaw, savedPitch;

    public FreeLook() {
        super("FreeLook", "Look around freely without moving player head", Category.RENDER, 0);
    }

    @Override
    public void onEnable() {
        GhostClient.getInstance().getEventBus().subscribe(this);
        if (mc.player != null) {
            savedYaw = mc.player.getYaw();
            savedPitch = mc.player.getPitch();
        }
    }

    @Override
    public void onDisable() {
        GhostClient.getInstance().getEventBus().unsubscribe(this);
        if (mc.player != null && mode.getValue().equals("Lock")) {
            mc.player.setYaw(savedYaw);
            mc.player.setPitch(savedPitch);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mode.getValue().equals("Lock")) {
            // Camera moves visually but server yaw stays at savedYaw
            // Implementation requires mixin to GameRenderer for full effect
        }
    }

    public float getSavedYaw() { return savedYaw; }
    public float getSavedPitch() { return savedPitch; }
}
