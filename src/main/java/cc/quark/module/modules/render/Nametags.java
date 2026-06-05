package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ColorUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class Nametags extends Module {

    private final BoolSetting showHealth = register(new BoolSetting(
            "Health", "Show entity health as hearts", true));

    private final BoolSetting showDistance = register(new BoolSetting(
            "Distance", "Show distance to entity", true));

    private final BoolSetting showArmor = register(new BoolSetting(
            "Armor", "Show armor durability for players", true));

    private final DoubleSetting scale = register(new DoubleSetting(
            "Scale", "Nametag text scale", 1.0, 0.5, 3.0));

    public Nametags() {
        super("Nametags", "Shows enhanced info above entity heads", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null || mc.gameRenderer == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isRemoved()) continue;

            renderNametag(event.getMatrixStack(), event.getTickDelta(), living);
        }
    }

    private void renderNametag(MatrixStack matrices, float tickDelta, LivingEntity entity) {
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        double ex = entity.prevX + (entity.getX() - entity.prevX) * tickDelta;
        double ey = entity.prevY + (entity.getY() - entity.prevY) * tickDelta;
        double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta;

        double tx = ex - camPos.x;
        double ty = ey + entity.getHeight() + 0.35 - camPos.y;
        double tz = ez - camPos.z;

        String name = entity.getDisplayName().getString();
        StringBuilder nameLine = new StringBuilder(name);

        int nameColor = 0xFFFFFFFF;
        if (entity instanceof PlayerEntity player) {
            float pct = player.getMaxHealth() > 0 ? player.getHealth() / player.getMaxHealth() : 1f;
            nameColor = ColorUtil.healthColor(pct) | 0xFF000000;
        }

        TextRenderer textRenderer = mc.textRenderer;
        float s = (float)(scale.get() * 0.025);

        matrices.push();
        matrices.translate(tx, ty, tz);
        matrices.multiply(camera.getRotation());
        matrices.scale(-s, -s, s);

        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();

        int nameW = textRenderer.getWidth(nameLine.toString());
        textRenderer.draw(
                nameLine.toString(),
                -nameW / 2f, 0f,
                nameColor,
                false,
                matrices.peek().getPositionMatrix(),
                immediate,
                TextRenderer.TextLayerType.SEE_THROUGH,
                0x55000000,
                0xF000F0
        );
        immediate.draw();

        int lineY = 10;

        if (showHealth.isEnabled()) {
            float hp = entity.getHealth();
            float maxHp = entity.getMaxHealth();
            float pct = maxHp > 0 ? hp / maxHp : 1f;
            int hc = ColorUtil.healthColor(pct) | 0xFF000000;
            int hearts = (int) Math.ceil(hp / 2f);
            StringBuilder hpSb = new StringBuilder();
            for (int i = 0; i < Math.min(hearts, 10); i++) hpSb.append('❤');
            String hpStr = hpSb + String.format(" %.1f", hp);
            int hpW = textRenderer.getWidth(hpStr);
            textRenderer.draw(
                    hpStr, -hpW / 2f, lineY,
                    hc, false,
                    matrices.peek().getPositionMatrix(),
                    immediate,
                    TextRenderer.TextLayerType.SEE_THROUGH,
                    0x44000000,
                    0xF000F0
            );
            immediate.draw();
            lineY += 10;
        }

        if (showDistance.isEnabled()) {
            double dist = mc.player.distanceTo(entity);
            String distStr = String.format("%.1fm", dist);
            int distW = textRenderer.getWidth(distStr);
            textRenderer.draw(
                    distStr, -distW / 2f, lineY,
                    0xFFAAAAAA,
                    false,
                    matrices.peek().getPositionMatrix(),
                    immediate,
                    TextRenderer.TextLayerType.SEE_THROUGH,
                    0x44000000,
                    0xF000F0
            );
            immediate.draw();
            lineY += 10;
        }

        if (showArmor.isEnabled() && entity instanceof PlayerEntity player) {
            renderArmorLine(matrices, textRenderer, immediate, player, lineY);
        }

        matrices.pop();
    }

    private void renderArmorLine(MatrixStack matrices, TextRenderer textRenderer,
                                  VertexConsumerProvider.Immediate immediate,
                                  PlayerEntity player, int lineY) {
        List<String> armorParts = new ArrayList<>();
        ItemStack[] armorSlots = {
                player.getInventory().getStack(36),
                player.getInventory().getStack(37),
                player.getInventory().getStack(38),
                player.getInventory().getStack(39)
        };

        for (ItemStack stack : armorSlots) {
            if (!stack.isEmpty() && stack.isDamageable()) {
                int maxDmg = stack.getMaxDamage();
                int dmg = stack.getDamage();
                int pct = maxDmg > 0 ? (int)(100f * (maxDmg - dmg) / maxDmg) : 100;
                String col = pct > 60 ? "§a" : pct > 30 ? "§e" : "§c";
                armorParts.add(col + pct + "%§r");
            }
        }

        if (armorParts.isEmpty()) return;

        String armorStr = String.join(" ", armorParts);
        int aw = textRenderer.getWidth(armorStr);
        textRenderer.draw(
                armorStr, -aw / 2f, lineY,
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
