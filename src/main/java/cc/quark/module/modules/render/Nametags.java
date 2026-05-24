package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Nametags - enhanced player nametags showing health, distance, armor, and ping.
 */
public class Nametags extends Module {

    private final BoolSetting showHealth = register(new BoolSetting(
            "Health", "Show entity health", true));

    private final BoolSetting showDistance = register(new BoolSetting(
            "Distance", "Show distance to entity", true));

    private final BoolSetting showArmor = register(new BoolSetting(
            "Armor", "Show armor durability", true));

    private final DoubleSetting scale = register(new DoubleSetting(
            "Scale", "Nametag text scale", 1.0, 0.5, 3.0));

    public Nametags() {
        super("Nametags", "Shows enhanced info above player heads", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null || mc.gameRenderer == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead()) continue;

            renderNametag(event.getMatrixStack(), event.getTickDelta(), living);
        }
    }

    private void renderNametag(MatrixStack matrices, float tickDelta, LivingEntity entity) {
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        // Interpolate entity position
        double ex = entity.prevX + (entity.getX() - entity.prevX) * tickDelta;
        double ey = entity.prevY + (entity.getY() - entity.prevY) * tickDelta;
        double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta;

        // Position nametag above the entity's head
        double tx = ex - camPos.x;
        double ty = ey + entity.getHeight() + 0.3 - camPos.y;
        double tz = ez - camPos.z;

        // Build the label
        StringBuilder sb = new StringBuilder();
        String name = entity.getDisplayName().getString();
        sb.append(name);

        if (showHealth.isEnabled() && entity instanceof LivingEntity le) {
            float hp = le.getHealth();
            // Color based on health percentage
            String hpStr = String.format("%.1f", hp);
            sb.append(" Â§c").append(hpStr).append("Â§r hp");
        }

        if (showDistance.isEnabled()) {
            double dist = mc.player.distanceTo(entity);
            sb.append(" Â§7").append(String.format("%.1f", dist)).append("mÂ§r");
        }

        String label = sb.toString();

        matrices.push();
        matrices.translate(tx, ty, tz);

        // Billboard: rotate to face camera
        matrices.multiply(camera.getRotation());
        float s = (float)(scale.get() * 0.025);
        matrices.scale(-s, -s, s);

        TextRenderer textRenderer = mc.textRenderer;
        int textWidth = textRenderer.getWidth(label);

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Draw background
        int x = -textWidth / 2;
        VertexConsumerProvider.Immediate immediate =
                mc.getBufferBuilders().getEntityVertexConsumers();
        textRenderer.draw(
                label,
                x, 0,
                0xFFFFFFFF,
                false,
                matrix,
                immediate,
                TextRenderer.TextLayerType.SEE_THROUGH,
                0x44000000,
                0xF000F0
        );
        immediate.draw();

        // Armor bar below name
        if (showArmor.isEnabled() && entity instanceof PlayerEntity player) {
            renderArmorInfo(matrices, textRenderer, player, textWidth);
        }

        matrices.pop();
    }

    private void renderArmorInfo(MatrixStack matrices, TextRenderer textRenderer,
                                  PlayerEntity player, int nameWidth) {
        List<String> armorParts = new ArrayList<>();
        ItemStack[] armor = new ItemStack[]{
                player.getInventory().getStack(36), // boots
                player.getInventory().getStack(37), // leggings
                player.getInventory().getStack(38), // chestplate
                player.getInventory().getStack(39)  // helmet
        };

        for (ItemStack stack : armor) {
            if (!stack.isEmpty() && stack.isDamageable()) {
                int maxDmg = stack.getMaxDamage();
                int dmg = stack.getDamage();
                int pct = (int)(100f * (maxDmg - dmg) / maxDmg);
                // Color: green > 60, yellow > 30, red otherwise
                String color = pct > 60 ? "Â§a" : pct > 30 ? "Â§e" : "Â§c";
                armorParts.add(color + pct + "%Â§r");
            }
        }

        if (!armorParts.isEmpty()) {
            String armorStr = String.join(" ", armorParts);
            int aw = textRenderer.getWidth(armorStr);
            VertexConsumerProvider.Immediate immediate =
                    mc.getBufferBuilders().getEntityVertexConsumers();
            textRenderer.draw(
                    armorStr,
                    -aw / 2, 10,
                    0xFFFFFFFF,
                    false,
                    matrices.peek().getPositionMatrix(),
                    immediate,
                    TextRenderer.TextLayerType.SEE_THROUGH,
                    0x44000000,
                    0xF000F0
            );
            immediate.draw();
        }
    }
}
