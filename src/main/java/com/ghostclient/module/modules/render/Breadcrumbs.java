package com.ghostclient.module.modules.render;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventRender3D;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.ColorSetting;
import com.ghostclient.setting.IntSetting;
import com.ghostclient.util.RenderUtil;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Breadcrumbs - draws a trail of lines behind the player showing their path.
 */
public class Breadcrumbs extends Module {

    private final IntSetting length = register(new IntSetting(
            "Length", "Maximum number of trail points to keep", 200, 50, 500));

    private final ColorSetting color = register(new ColorSetting(
            "Color", "Trail color (ARGB)", 0xFF00AAFF));

    private final BoolSetting fade = register(new BoolSetting(
            "Fade", "Fade trail from full opacity to transparent at the tail", true));

    private final Deque<Vec3d> trail = new ArrayDeque<>();

    public Breadcrumbs() {
        super("Breadcrumbs", "Renders a position trail behind the player", Category.RENDER);
    }

    @Override
    public void onEnable() {
        trail.clear();
    }

    @Override
    public void onDisable() {
        trail.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        Vec3d pos = mc.player.getPos();

        // Only add a new point if the player has moved at least 0.2 blocks
        if (!trail.isEmpty()) {
            Vec3d last = trail.peekLast();
            if (last.distanceTo(pos) < 0.2) return;
        }

        trail.addLast(pos.add(0, 0.1, 0)); // Slight Y offset to avoid clipping into ground

        // Trim to max length
        while (trail.size() > length.get()) {
            trail.pollFirst();
        }
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || trail.size() < 2) return;

        Vec3d[] points = trail.toArray(new Vec3d[0]);

        float baseR = color.getRedF();
        float baseG = color.getGreenF();
        float baseB = color.getBlueF();
        float baseA = color.getAlphaF();

        for (int i = 0; i < points.length - 1; i++) {
            float alpha;
            if (fade.isEnabled()) {
                // Newest point = full alpha, oldest = transparent
                alpha = baseA * ((float)(i + 1) / points.length);
            } else {
                alpha = baseA;
            }

            RenderUtil.drawLine3D(event.getMatrixStack(),
                    points[i], points[i + 1],
                    baseR, baseG, baseB, alpha, 1.5f);
        }
    }
}
