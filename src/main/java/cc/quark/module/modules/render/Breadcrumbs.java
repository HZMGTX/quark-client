package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;

public class Breadcrumbs extends Module {

    private final ColorSetting color = register(new ColorSetting("Color", "Trail color", 0xFF00AAFF));
    private final BoolSetting fade = register(new BoolSetting("Fade", "Fade older trail segments", true));
    private final BoolSetting showStart = register(new BoolSetting("Show Start", "Mark starting position with a box", true));

    private static final int MAX_POINTS = 200;

    private final Deque<Vec3d> trail = new ArrayDeque<>();
    private final TimerUtil timer = new TimerUtil();

    public Breadcrumbs() {
        super("Breadcrumbs", "Renders a position trail behind the player", Category.RENDER);
    }

    @Override
    public void onEnable() {
        trail.clear();
        timer.reset();
    }

    @Override
    public void onDisable() {
        trail.clear();
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null) return;

        if (timer.hasReached(500)) {
            timer.reset();
            Vec3d pos = mc.player.getPos().add(0, 0.05, 0);
            if (trail.isEmpty() || trail.peekLast().distanceTo(pos) >= 0.1) {
                trail.addLast(pos);
                while (trail.size() > MAX_POINTS) {
                    trail.pollFirst();
                }
            }
        }

        if (trail.size() < 2) return;

        MatrixStack matrices = event.getMatrixStack();
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
            RenderUtil.drawLine3D(matrices, points[i], points[i + 1], baseR, baseG, baseB, alpha, 1.5f);
        }

        if (showStart.isEnabled() && points.length > 0) {
            Vec3d start = points[0];
            Box startBox = new Box(start.x - 0.3, start.y - 0.05, start.z - 0.3,
                                   start.x + 0.3, start.y + 0.05, start.z + 0.3);
            RenderUtil.drawFilledBox(matrices, startBox, 1.0f, 0.5f, 0.0f, 0.45f);
            RenderUtil.drawESPBox(matrices, startBox, 1.0f, 0.5f, 0.0f, 0.9f, 1.5f);
        }
    }
}
