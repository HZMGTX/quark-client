package com.ghostclient.module.modules.movement;

import com.ghostclient.GhostClient;
import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.ModeSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class Phase extends Module {

    private final ModeSetting mode = register(new ModeSetting("Mode", "Phase mode", "Gate", "Gate", "Clip"));

    public Phase() {
        super("Phase", "Phase through blocks", Category.MOVEMENT, 0);
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
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (mode.getValue().equals("Clip")) {
            Vec3d pos = mc.player.getPos();
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y + 0.0625, pos.z, false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y, pos.z, false));
        }
    }
}
