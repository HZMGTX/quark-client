package cc.quark.module.modules.render;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import cc.quark.waypoint.WaypointManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * WaypointESP — renders 3D beacons and 2D distance labels at each waypoint
 * stored in the WaypointManager. Supports per-waypoint color from the manager.
 */
public class WaypointESP extends Module {

    private final BoolSetting show3DBeacon = register(new BoolSetting(
            "3D Beacon", "Draw a 3D column/box at the waypoint location", true));

    private final BoolSetting showLabel = register(new BoolSetting(
            "Label", "Show waypoint name and distance as a 2D label", true));

    private final BoolSetting showDistance = register(new BoolSetting(
            "Distance", "Include distance in the waypoint label", true));

    private final DoubleSetting maxRange = register(new DoubleSetting(
            "Max Range", "Maximum display distance in blocks (0 = unlimited)", 0, 0, 5000));

    private final IntSetting beaconHeight = register(new IntSetting(
            "Beacon Height", "Height of the 3D beacon column in blocks", 32, 4, 256));

    public WaypointESP() {
        super("WaypointESP", "Shows custom waypoints in the world", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        if (!show3DBeacon.isEnabled()) return;

        List<WaypointManager.Waypoint> waypoints =
                Quark.getInstance().getWaypointManager().getWaypoints();

        MatrixStack matrices = event.getMatrixStack();

        for (WaypointManager.Waypoint wp : waypoints) {
            double dist = mc.player.getPos().distanceTo(new Vec3d(wp.x(), wp.y(), wp.z()));
            if (maxRange.get() > 0 && dist > maxRange.get()) continue;

            float r = ((wp.color() >> 16) & 0xFF) / 255f;
            float g = ((wp.color() >> 8) & 0xFF) / 255f;
            float b = (wp.color() & 0xFF) / 255f;

            // Draw a tall column above the waypoint
            Box column = new Box(
                    wp.x() - 0.1, wp.y(),
                    wp.z() - 0.1,
                    wp.x() + 0.1, wp.y() + beaconHeight.get(),
                    wp.z() + 0.1);
            RenderUtil.drawESPBox(matrices, column, r, g, b, 0.9f, 1.5f);
            RenderUtil.drawFilledBox(matrices, column, r, g, b, 0.15f);

            // Small marker box at waypoint position
            Box marker = new Box(wp.x() - 0.5, wp.y() - 0.5, wp.z() - 0.5,
                    wp.x() + 0.5, wp.y() + 0.5, wp.z() + 0.5);
            RenderUtil.drawFilledBox(matrices, marker, r, g, b, 0.35f);
            RenderUtil.drawESPBox(matrices, marker, r, g, b, 1.0f, 2.0f);
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;
        if (!showLabel.isEnabled()) return;

        List<WaypointManager.Waypoint> waypoints =
                Quark.getInstance().getWaypointManager().getWaypoints();

        DrawContext ctx = event.getDrawContext();

        for (WaypointManager.Waypoint wp : waypoints) {
            Vec3d wpPos = new Vec3d(wp.x(), wp.y() + beaconHeight.get() * 0.5, wp.z());
            double dist = mc.player.getPos().distanceTo(new Vec3d(wp.x(), wp.y(), wp.z()));
            if (maxRange.get() > 0 && dist > maxRange.get()) continue;

            // Build label text
            String label = wp.name();
            if (showDistance.isEnabled()) {
                label += " " + (int) dist + "m";
            }

            // Project world pos to screen
            Camera camera = mc.gameRenderer.getCamera();
            Vec3d camPos = camera.getPos();
            double dx = wpPos.x - camPos.x;
            double dy = wpPos.y - camPos.y;
            double dz = wpPos.z - camPos.z;

            // Behind-camera check: projectToScreen returns null if rz2 <= 0

            // Use RenderUtil draw for 2D screen text — derive screen coords via
            // simple perspective projection
            int screenW = mc.getWindow().getScaledWidth();
            int screenH = mc.getWindow().getScaledHeight();

            double[] screenPos = projectToScreen(dx, dy, dz, screenW, screenH);
            if (screenPos == null) continue;

            int sx = (int) screenPos[0];
            int sy = (int) screenPos[1];
            if (sx < 0 || sy < 0 || sx > screenW || sy > screenH) continue;

            int textW = mc.textRenderer.getWidth(label);
            int color = wp.color() | 0xFF000000;

            ctx.fill(sx - textW / 2 - 3, sy - 2, sx + textW / 2 + 3,
                    sy + mc.textRenderer.fontHeight + 2, 0x88000000);
            RenderUtil.drawCustomText(ctx, label, sx - textW / 2, sy, color);
        }
    }

    /**
     * Very simple perspective projection from camera-relative world coords to
     * screen pixel coordinates. Returns null if the point is behind the camera.
     */
    private double[] projectToScreen(double dx, double dy, double dz, int screenW, int screenH) {
        double fov = Math.toRadians(mc.options.getFov().getValue());
        double aspect = (double) screenW / screenH;

        // Transform into camera space using camera's yaw/pitch
        Camera camera = mc.gameRenderer.getCamera();
        float yaw = camera.getYaw();
        float pitch = camera.getPitch();

        double sinYaw = Math.sin(Math.toRadians(yaw));
        double cosYaw = Math.cos(Math.toRadians(yaw));
        double sinPitch = Math.sin(Math.toRadians(pitch));
        double cosPitch = Math.cos(Math.toRadians(pitch));

        // Rotate around Y axis (yaw)
        double rx = dx * cosYaw - dz * sinYaw;
        double ry = dy;
        double rz = dx * sinYaw + dz * cosYaw;

        // Rotate around X axis (pitch)
        double rx2 = rx;
        double ry2 = ry * cosPitch - rz * sinPitch;
        double rz2 = ry * sinPitch + rz * cosPitch;

        if (rz2 <= 0) return null; // behind camera

        double tanHalfFov = Math.tan(fov / 2.0);
        double ndcX = -rx2 / (rz2 * tanHalfFov * aspect);
        double ndcY = -ry2 / (rz2 * tanHalfFov);

        double screenX = (ndcX + 1.0) * 0.5 * screenW;
        double screenY = (1.0 - ndcY) * 0.5 * screenH;
        return new double[]{screenX, screenY};
    }
}
