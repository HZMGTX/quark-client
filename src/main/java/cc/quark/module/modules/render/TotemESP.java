package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;

public class TotemESP extends Module {

    private final ColorSetting color = register(new ColorSetting("Color", "Highlight color for totem holders", 0xFFFFDD00));

    public TotemESP() {
        super("TotemESP", "Highlights players holding a totem of undying", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity p)) continue;
            if (e == mc.player) continue;

            boolean hasTotem = p.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING)
                    || p.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
            if (!hasTotem) continue;

            double ex = e.prevX + (e.getX() - e.prevX) * td;
            double ey = e.prevY + (e.getY() - e.prevY) * td;
            double ez = e.prevZ + (e.getZ() - e.prevZ) * td;
            Box box = e.getBoundingBox().offset(ex - e.getX(), ey - e.getY(), ez - e.getZ());
            RenderUtil.drawESPBox(m, box, color.getRedF(), color.getGreenF(), color.getBlueF(), color.getAlphaF(), 2.0f);
            RenderUtil.drawFilledBox(m, box, color.getRedF(), color.getGreenF(), color.getBlueF(), color.getAlphaF() * 0.15f);
        }
    }
}
