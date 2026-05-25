package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.Box;

public class MobESP extends Module {

    private final BoolSetting hostile  = register(new BoolSetting("Hostile",  "Highlight hostile mobs",  true));
    private final BoolSetting passive  = register(new BoolSetting("Passive",  "Highlight passive mobs",  false));
    private final BoolSetting villager = register(new BoolSetting("Villager", "Highlight villagers",     false));
    private final DoubleSetting fillAlpha = register(new DoubleSetting(
            "Fill Alpha", "Fill transparency (0–255)", 20, 0, 255));

    public MobESP() {
        super("MobESP", "Draws colored ESP boxes around mobs in the world", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        MatrixStack matrices = event.getMatrixStack();
        float tickDelta = event.getTickDelta();
        float fa = (float) (fillAlpha.get() / 255.0);

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;

            float r, g, b;
            if (hostile.isEnabled() && entity instanceof HostileEntity) {
                r = 1f; g = 0.2f; b = 0.2f;
            } else if (passive.isEnabled() && entity instanceof AnimalEntity) {
                r = 0.2f; g = 1f; b = 0.2f;
            } else if (villager.isEnabled() && entity instanceof VillagerEntity) {
                r = 1f; g = 0.85f; b = 0.2f;
            } else {
                continue;
            }

            double ex = entity.prevX + (entity.getX() - entity.prevX) * tickDelta;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * tickDelta;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta;
            Box box = entity.getBoundingBox().offset(
                    ex - entity.getX(), ey - entity.getY(), ez - entity.getZ());

            RenderUtil.drawESPBox(matrices, box, r, g, b, 0.85f, 1.2f);
            if (fa > 0) RenderUtil.drawFilledBox(matrices, box, r, g, b, fa);
        }
    }
}
