package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ItemFrameESP extends Module {

    private final BoolSetting showContents = register(new BoolSetting("ShowContents", "Show item name above item frames", true));

    public ItemFrameESP() {
        super("ItemFrameESP", "ESP for item frames with optional item name label", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemFrameEntity frame)) continue;
            double ex = frame.prevX + (frame.getX() - frame.prevX) * td;
            double ey = frame.prevY + (frame.getY() - frame.prevY) * td;
            double ez = frame.prevZ + (frame.getZ() - frame.prevZ) * td;
            Box box = frame.getBoundingBox().offset(ex - frame.getX(), ey - frame.getY(), ez - frame.getZ());
            RenderUtil.drawESPBox(m, box, 0.8f, 0.5f, 0.1f, 0.9f, 1.2f);
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showContents.isEnabled() || mc.world == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemFrameEntity frame)) continue;
            ItemStack held = frame.getHeldItemStack();
            if (held.isEmpty()) continue;
            Vec3d pos = new Vec3d(frame.getX(), frame.getY() + frame.getHeight() + 0.3, frame.getZ());
            double[] screen = RenderUtil.project(pos);
            if (screen == null) continue;
            String name = held.getName().getString();
            int tx = (int) screen[0] - mc.textRenderer.getWidth(name) / 2;
            ctx.drawTextWithShadow(mc.textRenderer, name, tx, (int) screen[1], 0xFFFFAA44);
        }
    }
}
