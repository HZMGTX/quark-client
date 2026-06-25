package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class NametagESP extends Module {

    private final DoubleSetting range    = register(new DoubleSetting("Range",    "Max range in blocks",         64.0, 4.0, 256.0));
    private final BoolSetting health     = register(new BoolSetting("Health",     "Show player health",          true));
    private final BoolSetting ping       = register(new BoolSetting("Ping",       "Show network ping",           true));
    private final BoolSetting distance   = register(new BoolSetting("Distance",   "Show distance to player",     true));

    public NametagESP() {
        super("NametagESP", "Enhanced nametags with more info", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();

        List<PlayerEntity> players = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity player)) continue;
            double dist = mc.player.distanceTo(player);
            if (dist > range.get()) continue;
            players.add(player);
        }

        for (PlayerEntity player : players) {
            int[] screen = projectToScreen(player.getX(), player.getY() + player.getHeight() + 0.3, player.getZ());
            if (screen == null) continue;

            StringBuilder sb = new StringBuilder();
            sb.append(player.getGameProfile().getName());

            if (health.isEnabled()) {
                int hp = (int) Math.ceil(player.getHealth());
                sb.append(" §c").append(hp).append("hp");
            }

            if (ping.isEnabled() && mc.getNetworkHandler() != null) {
                var entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
                if (entry != null) {
                    int latency = entry.getLatency();
                    String pingStr = latency + "ms";
                    int pingColor = latency < 80 ? 0xFF55FF55 : (latency < 150 ? 0xFFFFAA00 : 0xFFFF5555);
                    sb.append(" §7[");
                    String full = sb.toString();
                    int tw = mc.textRenderer.getWidth(full + pingStr + "]");
                    ctx.drawTextWithShadow(mc.textRenderer, full + pingStr + "]", screen[0] - tw / 2, screen[1], 0xFFFFFFFF);
                    continue;
                }
            }

            if (distance.isEnabled()) {
                double dist = mc.player.distanceTo(player);
                sb.append(String.format(" §7%.0fm", dist));
            }

            String label = sb.toString().replace("§c", "").replace("§7", "").replace("§a", "");
            int tw = mc.textRenderer.getWidth(label);
            // Background
            ctx.fill(screen[0] - tw / 2 - 1, screen[1] - 1, screen[0] + tw / 2 + 1, screen[1] + 9, 0x88000000);
            ctx.drawTextWithShadow(mc.textRenderer, label, screen[0] - tw / 2, screen[1], 0xFFFFFFFF);
        }
    }

    private int[] projectToScreen(double wx, double wy, double wz) {
        try {
            net.minecraft.client.render.Camera cam = mc.gameRenderer.getCamera();
            Vec3d camPos = cam.getPos();

            double dx = wx - camPos.x;
            double dy = wy - camPos.y;
            double dz = wz - camPos.z;

            double yawRad   = Math.toRadians(cam.getYaw());
            double pitchRad = Math.toRadians(cam.getPitch());

            double cosYaw   = Math.cos(yawRad);
            double sinYaw   = Math.sin(yawRad);
            double cosPitch = Math.cos(pitchRad);
            double sinPitch = Math.sin(pitchRad);

            double rx =  dx * cosYaw - dz * sinYaw;
            double ry =  dx * sinYaw * sinPitch + dy * cosPitch + dz * cosYaw * sinPitch;
            double rz = -dx * sinYaw * cosPitch + dy * sinPitch + dz * cosYaw * cosPitch;

            if (rz >= 0) return null; // Behind camera

            int sw = mc.getWindow().getScaledWidth();
            int sh = mc.getWindow().getScaledHeight();
            double fovRad = Math.toRadians(mc.options.getFov().getValue());
            double aspect = (double) sw / sh;

            int sx = (int)(sw / 2 + (rx / (-rz)) * (sw / (2 * Math.tan(fovRad / 2))));
            int sy = (int)(sh / 2 - (ry / (-rz)) * (sh / (2 * Math.tan(fovRad / 2) / aspect)));

            return new int[]{sx, sy};
        } catch (Exception e) {
            return null;
        }
    }
}
