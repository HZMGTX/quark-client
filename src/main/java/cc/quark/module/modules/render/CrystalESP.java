package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.Box;

public class CrystalESP extends Module {

    private final ColorSetting color  = register(new ColorSetting("Color", "ESP color for end crystals", 0xFFFF00FF));
    private final BoolSetting filled  = register(new BoolSetting("Filled", "Draw a translucent fill inside the box", true));

    public CrystalESP() {
        super("CrystalESP", "Highlights end crystals through walls with colored boxes", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        float r = color.getRedF();
        float g = color.getGreenF();
        float b = color.getBlueF();
        float td = event.getTickDelta();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;

            // Interpolated position
            double ex = crystal.prevX + (crystal.getX() - crystal.prevX) * td;
            double ey = crystal.prevY + (crystal.getY() - crystal.prevY) * td;
            double ez = crystal.prevZ + (crystal.getZ() - crystal.prevZ) * td;
            Box box = crystal.getBoundingBox().offset(
                    ex - crystal.getX(), ey - crystal.getY(), ez - crystal.getZ());

            RenderUtil.drawESPBox(event.getMatrixStack(), box, r, g, b, 0.9f, 1.5f);
            if (filled.isEnabled()) {
                RenderUtil.drawFilledBox(event.getMatrixStack(), box, r, g, b, 0.15f);
            }
        }
    }
}
