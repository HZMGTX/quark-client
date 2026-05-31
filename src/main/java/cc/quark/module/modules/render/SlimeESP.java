package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.util.math.Box;

public class SlimeESP extends Module {

    private final IntSetting minSize = register(new IntSetting("MinSize", "Minimum slime size to highlight", 1, 1, 4));

    public SlimeESP() {
        super("SlimeESP", "Highlights slimes and magma cubes by size", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();
        int min = minSize.get();

        for (Entity entity : mc.world.getEntities()) {
            float r, g, b;
            int size;
            if (entity instanceof SlimeEntity slime) {
                size = slime.getSize();
                r = 0.2f; g = 0.9f; b = 0.2f;
            } else if (entity instanceof MagmaCubeEntity cube) {
                size = cube.getSize();
                r = 1f; g = 0.3f; b = 0f;
            } else {
                continue;
            }
            if (size < min) continue;
            float alpha = 0.5f + 0.15f * size;
            double ex = entity.prevX + (entity.getX() - entity.prevX) * td;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * td;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * td;
            Box box = entity.getBoundingBox().offset(ex - entity.getX(), ey - entity.getY(), ez - entity.getZ());
            RenderUtil.drawESPBox(m, box, r, g, b, Math.min(alpha, 1f), 1.5f);
            RenderUtil.drawFilledBox(m, box, r, g, b, 0.1f);
        }
    }
}
