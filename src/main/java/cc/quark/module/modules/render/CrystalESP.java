package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.gui.ClickGUI;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ColorUtil;
import cc.quark.util.RenderUtil;
import net.minecraft.client.render.*;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class CrystalESP extends Module {

    private final ColorSetting color = register(new ColorSetting(
            "Color", "ESP color for end crystals", 0xFFFF00FF));
    private final DoubleSetting fillAlpha = register(new DoubleSetting(
            "Fill Alpha", "Fill transparency (0-255)", 30, 0, 255));
    private final DoubleSetting lineWidth = register(new DoubleSetting(
            "Line Width", "Outline thickness", 1.5, 0.5, 4.0));
    private final BoolSetting glow = register(new BoolSetting(
            "Glow", "Draw a wider glow outline using accent color", true));
    private final BoolSetting damagePreview = register(new BoolSetting(
            "Damage Preview", "Show predicted damage above each crystal", false));
    private final DoubleSetting damageRange = register(new DoubleSetting(
            "Damage Range", "Explosion damage radius", 6.0, 1.0, 12.0));

    public CrystalESP() {
        super("CrystalESP", "Draws ESP boxes around End Crystals in the world", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        MatrixStack matrices = event.getMatrixStack();
        float r = color.getRedF(), g = color.getGreenF(), b = color.getBlueF();
        float fa = (float) (fillAlpha.get() / 255.0);
        float tickDelta = event.getTickDelta();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;

            double ex = crystal.prevX + (crystal.getX() - crystal.prevX) * tickDelta;
            double ey = crystal.prevY + (crystal.getY() - crystal.prevY) * tickDelta;
            double ez = crystal.prevZ + (crystal.getZ() - crystal.prevZ) * tickDelta;
            Box box = crystal.getBoundingBox().offset(
                    ex - crystal.getX(), ey - crystal.getY(), ez - crystal.getZ());

            if (glow.isEnabled()) {
                int accent = ClickGUI.getAccentColor();
                float gr = ((accent >> 16) & 0xFF) / 255f;
                float gg = ((accent >> 8) & 0xFF) / 255f;
                float gb = (accent & 0xFF) / 255f;
                Box glowBox = box.expand(0.07);
                RenderUtil.drawESPBox(matrices, glowBox, gr, gg, gb, 0.5f, (float) lineWidth.get() + 1.5f);
            }

            RenderUtil.drawESPBox(matrices, box, r, g, b, 0.9f, (float) lineWidth.get());
            if (fa > 0) RenderUtil.drawFilledBox(matrices, box, r, g, b, fa);

            if (damagePreview.isEnabled()) {
                double maxDmg = estimateMaxDamage(new Vec3d(ex, ey, ez));
                if (maxDmg > 0) {
                    renderDamageLabel(matrices, crystal, ex, ey, ez, maxDmg, tickDelta);
                }
            }
        }
    }

    private double estimateMaxDamage(Vec3d crystalPos) {
        double maxDmg = 0;
        double radius = damageRange.get();
        if (mc.world == null) return 0;
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            double dist = entity.getPos().distanceTo(crystalPos);
            if (dist > radius) continue;
            double exposure = 1.0 - (dist / radius);
            double baseDmg = (exposure * exposure + exposure) / 2.0 * 7 * radius * 1.5;
            if (entity == mc.player) {
                maxDmg = Math.max(maxDmg, baseDmg);
            }
        }
        return maxDmg;
    }

    private void renderDamageLabel(MatrixStack matrices, EndCrystalEntity crystal,
                                    double ex, double ey, double ez, double damage, float tickDelta) {
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        double tx = ex - camPos.x;
        double ty = ey + crystal.getHeight() + 0.3 - camPos.y;
        double tz = ez - camPos.z;

        String label = String.format("§c%.1f dmg§r", damage);
        float hpPct = mc.player != null && mc.player.getMaxHealth() > 0
                ? (float)(damage / mc.player.getMaxHealth()) : 0f;
        hpPct = Math.min(1f, (float) hpPct);
        int dmgColor = ColorUtil.healthColor(hpPct) | 0xFF000000;

        matrices.push();
        matrices.translate(tx, ty, tz);
        matrices.multiply(camera.getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);

        int tw = mc.textRenderer.getWidth(label);
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        mc.textRenderer.draw(
                label, -tw / 2f, 0f,
                dmgColor, false,
                matrices.peek().getPositionMatrix(),
                immediate,
                net.minecraft.client.font.TextRenderer.TextLayerType.SEE_THROUGH,
                0x44000000,
                0xF000F0
        );
        immediate.draw();
        matrices.pop();
    }
}
