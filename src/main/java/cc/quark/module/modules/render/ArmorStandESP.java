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
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ArmorStandESP extends Module {

    private final BoolSetting showItems = register(new BoolSetting("ShowItems", "List armor stand equipment above it", true));

    public ArmorStandESP() {
        super("ArmorStandESP", "ESP for armor stands with armor, with optional item set info", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ArmorStandEntity stand)) continue;
            boolean hasArmor = false;
            for (ItemStack s : stand.getArmorItems()) {
                if (!s.isEmpty()) { hasArmor = true; break; }
            }
            if (!hasArmor) continue;
            double ex = stand.prevX + (stand.getX() - stand.prevX) * td;
            double ey = stand.prevY + (stand.getY() - stand.prevY) * td;
            double ez = stand.prevZ + (stand.getZ() - stand.prevZ) * td;
            Box box = stand.getBoundingBox().offset(ex - stand.getX(), ey - stand.getY(), ez - stand.getZ());
            RenderUtil.drawESPBox(m, box, 0.9f, 0.8f, 0.2f, 0.9f, 1.2f);
            RenderUtil.drawFilledBox(m, box, 0.9f, 0.8f, 0.2f, 0.08f);
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showItems.isEnabled() || mc.world == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ArmorStandEntity stand)) continue;
            Vec3d pos = new Vec3d(stand.getX(), stand.getY() + stand.getHeight() + 0.3, stand.getZ());
            double[] screen = RenderUtil.project(pos);
            if (screen == null) continue;
            int tx = (int) screen[0];
            int ty = (int) screen[1];
            int lh = mc.textRenderer.fontHeight + 1;
            for (ItemStack s : stand.getArmorItems()) {
                if (s.isEmpty()) continue;
                String name = s.getName().getString();
                ctx.drawTextWithShadow(mc.textRenderer, name, tx - mc.textRenderer.getWidth(name) / 2, ty, 0xFFFFDD55);
                ty += lh;
            }
        }
    }
}
