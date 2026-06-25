package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.math.Box;

public class PetESP extends Module {

    private final ColorSetting color = register(new ColorSetting("Color", "Pet ESP color", 0xFF00FFAA));

    public PetESP() {
        super("PetESP", "Highlights player-owned tamed animals (pets)", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();
        float r = color.getRedF(), g = color.getGreenF(), b = color.getBlueF(), a = color.getAlphaF();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof TameableEntity tameable)) continue;
            if (!tameable.isTamed()) continue;
            if (tameable.getOwner() != mc.player) continue;
            double ex = tameable.prevX + (tameable.getX() - tameable.prevX) * td;
            double ey = tameable.prevY + (tameable.getY() - tameable.prevY) * td;
            double ez = tameable.prevZ + (tameable.getZ() - tameable.prevZ) * td;
            Box box = tameable.getBoundingBox().offset(ex - tameable.getX(), ey - tameable.getY(), ez - tameable.getZ());
            RenderUtil.drawESPBox(m, box, r, g, b, a, 1.5f);
            RenderUtil.drawFilledBox(m, box, r, g, b, 0.12f);
        }
    }
}
