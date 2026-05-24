package com.ghostclient.module.modules.movement;

import com.ghostclient.GhostClient;
import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AntiVoid extends Module {

    private final DoubleSetting threshold = register(new DoubleSetting("Y Threshold", "Y level to activate", -10.0, -50.0, 0.0));
    private Vec3d lastSafePos;

    public AntiVoid() {
        super("AntiVoid", "Prevents falling into the void", Category.MOVEMENT, 0);
    }

    @Override
    public void onEnable() {
        GhostClient.getInstance().getEventBus().subscribe(this);
        if (mc.player != null) lastSafePos = mc.player.getPos();
    }

    @Override
    public void onDisable() {
        GhostClient.getInstance().getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        Vec3d pos = mc.player.getPos();

        if (mc.player.isOnGround()) {
            lastSafePos = pos;
        }

        if (pos.y < threshold.getValue() && lastSafePos != null) {
            mc.player.setPos(lastSafePos.x, lastSafePos.y, lastSafePos.z);
            mc.player.setVelocity(0, 0, 0);
        }

        if (pos.y < threshold.getValue() - 5) {
            mc.player.setVelocity(mc.player.getVelocity().x, 0.5, mc.player.getVelocity().z);
        }
    }
}
