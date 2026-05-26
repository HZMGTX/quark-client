package cc.quark.module.modules.misc;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RenderUtil;
import cc.quark.waypoint.WaypointManager;
import cc.quark.waypoint.WaypointManager.Waypoint;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Waypoints extends Module {

    private final BoolSetting espBoxes = register(new BoolSetting(
            "ESP Boxes", "Draw a box at each waypoint", true));

    private final BoolSetting tracers = register(new BoolSetting(
            "Tracers", "Draw a line from player to each waypoint", true));

    private final BoolSetting distanceText = register(new BoolSetting(
            "Distance Text", "Show waypoint name and distance on the HUD", true));

    private final DoubleSetting maxDistance = register(new DoubleSetting(
            "Max Distance", "Maximum distance to render waypoints (blocks)", 256, 16, 1024));

    public Waypoints() {
        super("Waypoints", "Renders waypoints in the world", Category.MISC);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;
        Quark q = Quark.getInstance();
        if (q == null) return;
        WaypointManager wm = q.getWaypointManager();
        if (wm == null) return;

        List<Waypoint> list = wm.getWaypoints();
        if (list.isEmpty()) return;

        Vec3d eyes = mc.player.getEyePos();
        double maxDist = maxDistance.get();

        for (Waypoint w : list) {
            double dist = Math.sqrt(
                    Math.pow(w.x() - mc.player.getX(), 2) +
                    Math.pow(w.y() - mc.player.getY(), 2) +
                    Math.pow(w.z() - mc.player.getZ(), 2));

            if (dist > maxDist) continue;

            float r = ((w.color() >> 16) & 0xFF) / 255f;
            float g = ((w.color() >> 8)  & 0xFF) / 255f;
            float b = (w.color()         & 0xFF) / 255f;

            if (espBoxes.isEnabled()) {
                Box box = new Box(w.x() - 0.5, w.y() - 0.5, w.z() - 0.5,
                                  w.x() + 0.5, w.y() + 0.5, w.z() + 0.5);
                RenderUtil.drawESPBox(event.getMatrixStack(), box, r, g, b, 0.9f, 1.5f);
            }

            if (tracers.isEnabled()) {
                Vec3d target = new Vec3d(w.x(), w.y(), w.z());
                RenderUtil.drawLine3D(event.getMatrixStack(), eyes, target, r, g, b, 0.7f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!distanceText.isEnabled()) return;
        if (mc.player == null) return;
        Quark q = Quark.getInstance();
        if (q == null) return;
        WaypointManager wm = q.getWaypointManager();
        if (wm == null) return;

        List<Waypoint> list = wm.getWaypoints();
        if (list.isEmpty()) return;

        DrawContext ctx = event.getDrawContext();
        double maxDist = maxDistance.get();
        int yOffset = 4;

        for (Waypoint w : list) {
            double dist = Math.sqrt(
                    Math.pow(w.x() - mc.player.getX(), 2) +
                    Math.pow(w.y() - mc.player.getY(), 2) +
                    Math.pow(w.z() - mc.player.getZ(), 2));

            if (dist > maxDist) continue;

            int textColor = (w.color() & 0x00FFFFFF) | 0xFF000000;
            String label = w.name() + " §f" + String.format("%.0fm", dist);
            ctx.drawTextWithShadow(mc.textRenderer, label, 4, yOffset, textColor);
            yOffset += mc.textRenderer.fontHeight + 2;
        }
    }
}
