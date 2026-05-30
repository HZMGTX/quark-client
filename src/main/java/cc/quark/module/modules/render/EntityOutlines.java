package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

public class EntityOutlines extends Module {

    private final BoolSetting players = register(new BoolSetting("Players", "Show outlines for other players", true));
    private final BoolSetting mobs    = register(new BoolSetting("Mobs",    "Show outlines for hostile mobs",  true));
    private final BoolSetting animals = register(new BoolSetting("Animals", "Show outlines for passive animals", false));
    private final ColorSetting color  = register(new ColorSetting("Color",  "Outline color", 0xFF00FF00));

    public EntityOutlines() {
        super("EntityOutlines", "Draws ESP outlines around entities in the world", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        MatrixStack matrices = event.getMatrixStack();
        float tickDelta = event.getTickDelta();
        float r = color.getRedF();
        float g = color.getGreenF();
        float b = color.getBlueF();
        float a = color.getAlphaF();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (entity.isInvisible()) continue;
            if (!(entity instanceof LivingEntity)) continue;

            boolean isPlayer = entity instanceof PlayerEntity;
            boolean isAnimal = entity instanceof AnimalEntity;
            boolean isMob    = entity instanceof MobEntity && !isAnimal;

            if (isPlayer && !players.isEnabled()) continue;
            if (isAnimal && !animals.isEnabled()) continue;
            if (isMob    && !mobs.isEnabled())    continue;
            if (!isPlayer && !isAnimal && !isMob) continue;

            double ex = entity.prevX + (entity.getX() - entity.prevX) * tickDelta;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * tickDelta;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta;
            Box box = entity.getBoundingBox().offset(
                    ex - entity.getX(), ey - entity.getY(), ez - entity.getZ());

            RenderUtil.drawESPBox(matrices, box, r, g, b, a, 1.5f);
        }
    }
}
