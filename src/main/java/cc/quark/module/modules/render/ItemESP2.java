package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * ItemESP2 — draws ESP boxes around dropped item entities and renders
 * item name labels with distance info.
 */
public class ItemESP2 extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Maximum display range in blocks", 64.0, 10.0, 256.0));

    private final BoolSetting showBox = register(new BoolSetting(
            "Box", "Draw 3D ESP box around item entity", true));

    private final BoolSetting showTracer = register(new BoolSetting(
            "Tracer", "Draw tracer line to item", false));

    private final BoolSetting showLabel = register(new BoolSetting(
            "Label", "Show item name and distance label", true));

    private final BoolSetting showDistance = register(new BoolSetting(
            "Distance", "Include distance in label", true));

    private final BoolSetting showCount = register(new BoolSetting(
            "Count", "Show item stack count in label", true));

    private final ColorSetting color = register(new ColorSetting(
            "Color", "ESP box color", 0xFFFFDD00));

    private final DoubleSetting fillAlpha = register(new DoubleSetting(
            "Fill Alpha", "Transparency of the fill (0=none)", 0.15, 0.0, 1.0));

    private final IntSetting maxItems = register(new IntSetting(
            "Max Items", "Max number of items to render", 64, 1, 256));

    public ItemESP2() {
        super("ItemESP2", "ESP for dropped items on the ground", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        MatrixStack matrices = event.getMatrixStack();
        float tickDelta = event.getTickDelta();
        float r = color.getRedF(), g = color.getGreenF(), b = color.getBlueF();
        float fa = (float) fillAlpha.get();

        int count = 0;
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity)) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;
            if (count++ >= maxItems.get()) break;

            // Interpolated position
            double ex = entity.prevX + (entity.getX() - entity.prevX) * tickDelta;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * tickDelta;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta;
            Box box = entity.getBoundingBox().offset(
                    ex - entity.getX(), ey - entity.getY(), ez - entity.getZ());

            if (showBox.isEnabled()) {
                if (fa > 0) RenderUtil.drawFilledBox(matrices, box, r, g, b, fa);
                RenderUtil.drawESPBox(matrices, box, r, g, b, 0.9f, 1.5f);
            }

            if (showTracer.isEnabled()) {
                Vec3d center = new Vec3d(ex, ey + entity.getHeight() * 0.5, ez);
                Vec3d eyes = mc.player.getEyePos();
                RenderUtil.drawLine3D(matrices, eyes, center, r, g, b, 0.7f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showLabel.isEnabled() || mc.world == null || mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        int textColor = color.get() | 0xFF000000;

        int count = 0;
        List<String[]> labels = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity ie)) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;
            if (count++ >= maxItems.get()) break;

            ItemStack stack = ie.getStack();
            StringBuilder sb = new StringBuilder(stack.getItem().getName().getString());
            if (showCount.isEnabled() && stack.getCount() > 1) sb.append(" x").append(stack.getCount());
            if (showDistance.isEnabled()) sb.append(String.format(" %.0fm", dist));

            double dx = entity.getX() - camPos.x;
            double dy = entity.getY() + entity.getHeight() * 0.5 - camPos.y;
            double dz = entity.getZ() - camPos.z;

            double fov = Math.toRadians(mc.options.getFov().getValue());
            double aspect = (double) screenW / screenH;
            double tanHalfFov = Math.tan(fov / 2.0);

            float yaw = camera.getYaw();
            float pitch = camera.getPitch();
            double sinY = Math.sin(Math.toRadians(yaw)), cosY = Math.cos(Math.toRadians(yaw));
            double sinP = Math.sin(Math.toRadians(pitch)), cosP = Math.cos(Math.toRadians(pitch));

            double rx = dx * cosY - dz * sinY;
            double ry = dy;
            double rz = dx * sinY + dz * cosY;
            double ry2 = ry * cosP - rz * sinP;
            double rz2 = ry * sinP + rz * cosP;

            if (rz2 <= 0) continue;

            double ndcX = -rx / (rz2 * tanHalfFov * aspect);
            double ndcY = -ry2 / (rz2 * tanHalfFov);
            int sx = (int)((ndcX + 1.0) * 0.5 * screenW);
            int sy = (int)((1.0 - ndcY) * 0.5 * screenH);
            if (sx < 0 || sy < 0 || sx > screenW || sy > screenH) continue;

            String label = sb.toString();
            int tw = mc.textRenderer.getWidth(label);
            ctx.fill(sx - tw / 2 - 2, sy - 1, sx + tw / 2 + 2, sy + mc.textRenderer.fontHeight + 1, 0x88000000);
            RenderUtil.drawCustomText(ctx, label, sx - tw / 2, sy, textColor);
        }
    }
}
