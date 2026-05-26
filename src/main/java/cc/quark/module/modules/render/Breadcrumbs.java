package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;

public class Breadcrumbs extends Module {

    private final IntSetting maxPoints = register(new IntSetting(
            "Max Points", "Maximum number of trail points to keep", 500, 100, 2000));

    private final IntSetting interval = register(new IntSetting(
            "Interval", "Record a point every N ticks", 2, 1, 20));

    private final ColorSetting color = register(new ColorSetting(
            "Color", "Trail color (ARGB)", 0xFF00AAFF));

    private final BoolSetting fade = register(new BoolSetting(
            "Fade", "Fade trail from full opacity at head to transparent at tail", true));

    private final BoolSetting showStart = register(new BoolSetting(
            "Show Start", "Mark the starting position with a box", true));

    private final Deque<Vec3d> trail = new ArrayDeque<>();
    private int tickCounter = 0;

    public Breadcrumbs() {
        super("Breadcrumbs", "Renders a position trail behind the player", Category.RENDER);
    }

    @Override
    public void onEnable() {
        trail.clear();
        tickCounter = 0;
    }

    @Override
    public void onDisable() {
        trail.clear();
        tickCounter = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        tickCounter++;
        if (tickCounter < interval.get()) return;
        tickCounter = 0;

        Vec3d pos = mc.player.getPos().add(0, 0.1, 0);

        if (!trail.isEmpty()) {
            Vec3d last = trail.peekLast();
            if (last.distanceTo(pos) < 0.1) return;
        }

        trail.addLast(pos);

        while (trail.size() > maxPoints.get()) {
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
                alpha = baseA * ((float)(i + 1) / points.length);
            } else {
                alpha = baseA;
            }

            RenderUtil.drawLine3D(event.getMatrixStack(),
                    points[i], points[i + 1],
                    baseR, baseG, baseB, alpha, 1.5f);
        }

        if (showStart.isEnabled() && points.length > 0) {
            Vec3d start = points[0];
            Box startBox = new Box(start.x - 0.3, start.y - 0.05, start.z - 0.3,
                                   start.x + 0.3, start.y + 0.05, start.z + 0.3);
            RenderUtil.drawFilledBox(event.getMatrixStack(), startBox, 1.0f, 0.5f, 0.0f, 0.6f);
            RenderUtil.drawESPBox(event.getMatrixStack(), startBox, 1.0f, 0.5f, 0.0f, 0.9f, 1.5f);
        }
    }
}
