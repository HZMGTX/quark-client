package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.*;

/**
 * TrailESP — draws a fading particle/line trail behind every visible player
 * (and optionally yourself) using stored position history.
 */
public class TrailESP extends Module {

    private final BoolSetting selfTrail = register(new BoolSetting(
            "Self Trail", "Draw a trail behind yourself too", false));

    private final IntSetting maxPoints = register(new IntSetting(
            "Points", "Number of trail history points to store per player", 40, 5, 200));

    private final DoubleSetting pointInterval = register(new DoubleSetting(
            "Interval", "Distance in blocks between recorded points", 0.3, 0.05, 2.0));

    private final DoubleSetting lineWidth = register(new DoubleSetting(
            "Line Width", "Thickness of the trail line", 2.0, 0.5, 5.0));

    private final ModeSetting colorMode = register(new ModeSetting(
            "Color", "Trail coloring mode", "Rainbow", "Rainbow", "Static", "Fade"));

    private final ColorSetting staticColor = register(new ColorSetting(
            "Static Color", "Color when Static mode is selected", 0xFFFF4444));

    // UUID -> deque of recorded positions
    private final Map<UUID, Deque<Vec3d>> trails = new HashMap<>();
    private final Map<UUID, Vec3d> lastRecorded = new HashMap<>();

    public TrailESP() {
        super("TrailESP", "Draws a particle trail behind players", Category.RENDER);
    }

    @Override
    public void onDisable() {
        trails.clear();
        lastRecorded.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player && !selfTrail.isEnabled()) continue;
            UUID id = player.getUuid();
            Vec3d pos = player.getPos();

            Vec3d last = lastRecorded.get(id);
            if (last != null && last.distanceTo(pos) < pointInterval.get()) continue;

            lastRecorded.put(id, pos);
            Deque<Vec3d> trail = trails.computeIfAbsent(id, k -> new ArrayDeque<>());
            trail.addLast(pos.add(0, 0.1, 0));
            while (trail.size() > maxPoints.get()) trail.pollFirst();
        }

        // Remove trails for players that have left
        trails.keySet().removeIf(id -> mc.world.getPlayers().stream().noneMatch(p -> p.getUuid().equals(id)));
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null || trails.isEmpty()) return;

        MatrixStack matrices = event.getMatrixStack();
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth((float) lineWidth.get());

        int playerIdx = 0;
        for (Map.Entry<UUID, Deque<Vec3d>> entry : trails.entrySet()) {
            Deque<Vec3d> points = entry.getValue();
            if (points.size() < 2) {
                playerIdx++;
                continue;
            }

            List<Vec3d> list = new ArrayList<>(points);
            matrices.push();

            MatrixStack.Entry me = matrices.peek();
            BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

            for (int i = 0; i < list.size() - 1; i++) {
                Vec3d a = list.get(i);
                Vec3d b = list.get(i + 1);

                float t = (float) i / (list.size() - 1);
                float[] rgb = resolveColor(t, playerIdx);
                float alpha = 0.15f + 0.85f * t; // fade near old end

                double ax = a.x - camPos.x, ay = a.y - camPos.y, az = a.z - camPos.z;
                double bx = b.x - camPos.x, by = b.y - camPos.y, bz = b.z - camPos.z;

                float dx = (float)(bx - ax), dy = (float)(by - ay), dz = (float)(bz - az);
                float len = (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
                if (len == 0) continue;
                float nx = dx/len, ny = dy/len, nz = dz/len;

                buf.vertex(me, (float)ax, (float)ay, (float)az)
                        .color(rgb[0], rgb[1], rgb[2], alpha * 0.5f)
                        .normal(me, nx, ny, nz);
                buf.vertex(me, (float)bx, (float)by, (float)bz)
                        .color(rgb[0], rgb[1], rgb[2], alpha)
                        .normal(me, nx, ny, nz);
            }

            BufferRenderer.drawWithGlobalProgram(buf.end());
            matrices.pop();
            playerIdx++;
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);
    }

    private float[] resolveColor(float t, int idx) {
        return switch (colorMode.get()) {
            case "Rainbow" -> {
                float hue = (System.currentTimeMillis() % 2000L) / 2000.0f + t * 0.3f + idx * 0.2f;
                int rgb = java.awt.Color.HSBtoRGB(hue % 1.0f, 1.0f, 1.0f);
                yield new float[]{((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f};
            }
            case "Fade" -> new float[]{1.0f - t, t * 0.5f, t};
            default -> new float[]{staticColor.getRedF(), staticColor.getGreenF(), staticColor.getBlueF()};
        };
    }
}
