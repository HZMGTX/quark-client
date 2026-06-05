package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class DeathPosition extends Module {

    private final IntSetting maxMarkers = register(new IntSetting("MaxMarkers", "Maximum number of death markers to store", 5, 1, 20));

    private final List<Vec3d> deathPositions = new ArrayList<>();
    private boolean wasDead = false;

    public DeathPosition() {
        super("DeathPosition", "Records and displays death positions as 3D markers in the world", Category.RENDER);
    }

    @Override
    public void onEnable() {
        deathPositions.clear();
        wasDead = false;
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        // Listen for respawn packet as indicator of death/respawn cycle
        if (event.getPacket() instanceof PlayerRespawnS2CPacket) {
            // Player is respawning — record last known position as death pos
            Vec3d pos = mc.player.getPos();
            recordDeath(pos);
        }
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        // Also check if player just died locally
        if (!wasDead && (mc.player.isRemoved() || mc.player.getHealth() <= 0f)) {
            wasDead = true;
            recordDeath(mc.player.getPos());
        } else if (wasDead && mc.player.getHealth() > 0f) {
            wasDead = false;
        }

        if (deathPositions.isEmpty()) return;

        MatrixStack matrices = event.getMatrixStack();
        Camera camera = mc.gameRenderer.getCamera();

        for (Vec3d pos : deathPositions) {
            // Draw a tall vertical pillar beacon effect
            Box pillarBox = new Box(pos.x - 0.1, pos.y, pos.z - 0.1,
                    pos.x + 0.1, pos.y + 3.0, pos.z + 0.1);
            RenderUtil.drawESPBox(matrices, pillarBox, 1.0f, 0.0f, 0.0f, 0.9f, 2.0f);
            RenderUtil.drawFilledBox(matrices, pillarBox, 1.0f, 0.0f, 0.0f, 0.15f);

            // Floating marker box at the top
            Box markerBox = new Box(pos.x - 0.4, pos.y + 3.0, pos.z - 0.4,
                    pos.x + 0.4, pos.y + 3.8, pos.z + 0.4);
            RenderUtil.drawESPBox(matrices, markerBox, 1.0f, 0.3f, 0.0f, 0.9f, 1.5f);
            RenderUtil.drawFilledBox(matrices, markerBox, 1.0f, 0.3f, 0.0f, 0.25f);
        }
    }

    private void recordDeath(Vec3d pos) {
        deathPositions.add(0, pos);
        int max = maxMarkers.get();
        while (deathPositions.size() > max) {
            deathPositions.remove(deathPositions.size() - 1);
        }
    }
}
