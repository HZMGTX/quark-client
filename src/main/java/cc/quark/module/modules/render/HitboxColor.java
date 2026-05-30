package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

public class HitboxColor extends Module {

    private final ColorSetting playerColor = register(new ColorSetting("PlayerColor", "Color for player hitboxes", 0xFF0000FF));
    private final ColorSetting mobColor = register(new ColorSetting("MobColor", "Color for hostile mob hitboxes", 0xFF00FF00));
    private final ColorSetting animalColor = register(new ColorSetting("AnimalColor", "Color for passive animal hitboxes", 0xFFFFFF00));
    private final DoubleSetting expand = register(new DoubleSetting("Expand", "Expand the bounding box outward", 0.0, 0.0, 0.5));

    public HitboxColor() {
        super("HitboxColor", "Draws colored bounding boxes around visible entities", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        MatrixStack matrices = event.getMatrixStack();
        float tickDelta = event.getTickDelta();
        double expandAmt = expand.get();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity)) continue;

            float r, g, b;
            if (entity instanceof PlayerEntity) {
                r = playerColor.getRedF();
                g = playerColor.getGreenF();
                b = playerColor.getBlueF();
            } else if (entity instanceof AnimalEntity) {
                r = animalColor.getRedF();
                g = animalColor.getGreenF();
                b = animalColor.getBlueF();
            } else if (entity instanceof MobEntity) {
                r = mobColor.getRedF();
                g = mobColor.getGreenF();
                b = mobColor.getBlueF();
            } else {
                continue;
            }

            double ex = entity.prevX + (entity.getX() - entity.prevX) * tickDelta;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * tickDelta;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta;

            Box box = entity.getBoundingBox()
                    .offset(ex - entity.getX(), ey - entity.getY(), ez - entity.getZ());

            if (expandAmt > 0) box = box.expand(expandAmt);

            RenderUtil.drawESPBox(matrices, box, r, g, b, 0.9f, 1.5f);
        }
    }
}
