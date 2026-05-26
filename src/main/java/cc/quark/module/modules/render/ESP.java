package cc.quark.module.modules.render;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ColorUtil;
import cc.quark.util.RenderUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class ESP extends Module {

    private final BoolSetting players  = register(new BoolSetting("Players",  "ESP for other players",          true));
    private final BoolSetting mobs     = register(new BoolSetting("Mobs",     "ESP for hostile mobs",           true));
    private final BoolSetting animals  = register(new BoolSetting("Animals",  "ESP for passive animals",        false));
    private final BoolSetting items    = register(new BoolSetting("Items",    "ESP for dropped items",          false));
    private final BoolSetting vehicles = register(new BoolSetting("Vehicles", "ESP for boats and minecarts",    false));

    private final ModeSetting mode     = register(new ModeSetting("Mode", "ESP draw mode", "Box", "Box", "Corner", "Tracer", "BoxTracer"));
    private final BoolSetting fill     = register(new BoolSetting("Fill",    "Fill the ESP box",             true));
    private final BoolSetting outline  = register(new BoolSetting("Outline", "Draw the ESP box outline",     true));
    private final BoolSetting glow     = register(new BoolSetting("Glow",    "Draw a wider glow outline",    false));
    private final BoolSetting showName = register(new BoolSetting("Names",   "Show entity name above box",   true));
    private final BoolSetting showDist = register(new BoolSetting("Distance","Show distance near box",       true));
    private final BoolSetting healthCol= register(new BoolSetting("HealthColor","Color players by health",   true));

    private final ColorSetting playerColor  = register(new ColorSetting("PlayerColor",  "Player ESP colour",  0xFFFF0000));
    private final ColorSetting mobColor     = register(new ColorSetting("MobColor",     "Mob ESP colour",     0xFFFF8800));
    private final ColorSetting friendColor  = register(new ColorSetting("FriendColor",  "Friend ESP colour",  0xFF00FF00));
    private final ColorSetting animalColor  = register(new ColorSetting("AnimalColor",  "Animal ESP colour",  0xFFFFFF00));

    private final DoubleSetting fillAlpha  = register(new DoubleSetting("FillAlpha",  "Fill transparency (0-255)", 30, 0, 255));

    public ESP() {
        super("ESP", "Draws boxes around entities through walls", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        MatrixStack matrices = event.getMatrixStack();
        float tickDelta = event.getTickDelta();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (entity.isInvisible()) continue;
            if (!(entity instanceof LivingEntity) && !(entity instanceof ItemEntity)
                    && !(entity instanceof BoatEntity) && !(entity instanceof MinecartEntity)) continue;

            float[] color = resolveColorRGB(entity);
            if (color == null) continue;

            float r = color[0], g = color[1], b = color[2];
            float fa = (float)(fillAlpha.get() / 255.0);

            double ex = entity.prevX + (entity.getX() - entity.prevX) * tickDelta;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * tickDelta;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta;
            Box box = entity.getBoundingBox().offset(
                    ex - entity.getX(), ey - entity.getY(), ez - entity.getZ());

            String modeVal = mode.get();

            if (modeVal.equals("Box") || modeVal.equals("BoxTracer")) {
                if (glow.isEnabled()) {
                    int accentArgb = cc.quark.gui.ClickGUI.getAccentColor();
                    float gr = ((accentArgb >> 16) & 0xFF) / 255f;
                    float gg = ((accentArgb >> 8) & 0xFF) / 255f;
                    float gb = (accentArgb & 0xFF) / 255f;
                    Box glowBox = box.expand(0.05);
                    RenderUtil.drawESPBox(matrices, glowBox, gr, gg, gb, 0.5f, 3.0f);
                }
                if (fill.isEnabled()) RenderUtil.drawFilledBox(matrices, box, r, g, b, fa);
                if (outline.isEnabled()) RenderUtil.drawESPBox(matrices, box, r, g, b, 0.9f, 1.5f);
            } else if (modeVal.equals("Corner")) {
                if (glow.isEnabled()) {
                    int accentArgb = cc.quark.gui.ClickGUI.getAccentColor();
                    float gr = ((accentArgb >> 16) & 0xFF) / 255f;
                    float gg = ((accentArgb >> 8) & 0xFF) / 255f;
                    float gb = (accentArgb & 0xFF) / 255f;
                    Box glowBox = box.expand(0.05);
                    drawCornerBox(matrices, glowBox, gr, gg, gb, 0.5f, 3.0f);
                }
                if (fill.isEnabled()) RenderUtil.drawFilledBox(matrices, box, r, g, b, fa);
                if (outline.isEnabled()) drawCornerBox(matrices, box, r, g, b, 0.9f, 1.5f);
            }

            if (modeVal.equals("Tracer") || modeVal.equals("BoxTracer")) {
                Vec3d center = new Vec3d(ex, ey + entity.getHeight() * 0.5, ez);
                Vec3d eyes = mc.player.getEyePos();
                RenderUtil.drawLine3D(matrices, eyes, center, r, g, b, 0.7f, 1.0f);
            }

            if (showName.isEnabled() || showDist.isEnabled()) {
                renderLabel(matrices, entity, ex, ey, ez, r, g, b);
            }
        }
    }

    private void renderLabel(MatrixStack matrices, Entity entity,
                              double ex, double ey, double ez,
                              float r, float g, float b) {
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        double tx = ex - camPos.x;
        double ty = ey + entity.getHeight() + 0.25 - camPos.y;
        double tz = ez - camPos.z;

        StringBuilder sb = new StringBuilder();
        if (showName.isEnabled()) {
            sb.append(entity.getDisplayName().getString());
        }
        if (showDist.isEnabled()) {
            double dist = mc.player.distanceTo(entity);
            if (sb.length() > 0) sb.append(" ");
            sb.append(String.format("%.0fm", dist));
        }
        String label = sb.toString();
        if (label.isEmpty()) return;

        matrices.push();
        matrices.translate(tx, ty, tz);
        matrices.multiply(camera.getRotation());
        float s = 0.025f;
        matrices.scale(-s, -s, s);

        int textColor = (int)(r * 255) << 16 | (int)(g * 255) << 8 | (int)(b * 255) | 0xFF000000;
        int tw = mc.textRenderer.getWidth(label);
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        mc.textRenderer.draw(
                label, -tw / 2f, 0f,
                textColor, false,
                matrices.peek().getPositionMatrix(),
                immediate,
                TextRenderer.TextLayerType.SEE_THROUGH,
                0x55000000,
                0xF000F0
        );
        immediate.draw();
        matrices.pop();
    }

    private float[] resolveColorRGB(Entity entity) {
        if (entity instanceof PlayerEntity player) {
            if (!players.isEnabled()) return null;
            String name = player.getGameProfile().getName();
            if (Quark.getInstance().getFriendManager().isFriend(name)) {
                return new float[]{friendColor.getRedF(), friendColor.getGreenF(), friendColor.getBlueF()};
            }
            if (healthCol.isEnabled()) {
                float maxHp = player.getMaxHealth();
                float hp = player.getHealth();
                float pct = maxHp > 0 ? hp / maxHp : 1f;
                int hc = ColorUtil.healthColor(pct);
                return new float[]{((hc >> 16) & 0xFF) / 255f, ((hc >> 8) & 0xFF) / 255f, (hc & 0xFF) / 255f};
            }
            return new float[]{playerColor.getRedF(), playerColor.getGreenF(), playerColor.getBlueF()};
        }
        if (entity instanceof AnimalEntity) {
            if (!animals.isEnabled()) return null;
            return new float[]{animalColor.getRedF(), animalColor.getGreenF(), animalColor.getBlueF()};
        }
        if (entity instanceof MobEntity) {
            if (!mobs.isEnabled()) return null;
            return new float[]{mobColor.getRedF(), mobColor.getGreenF(), mobColor.getBlueF()};
        }
        if (entity instanceof ItemEntity) {
            if (!items.isEnabled()) return null;
            return new float[]{animalColor.getRedF(), animalColor.getGreenF(), animalColor.getBlueF()};
        }
        if (entity instanceof BoatEntity || entity instanceof MinecartEntity) {
            if (!vehicles.isEnabled()) return null;
            return new float[]{mobColor.getRedF(), mobColor.getGreenF(), mobColor.getBlueF()};
        }
        return null;
    }

    private void drawCornerBox(MatrixStack matrices, Box box, float r, float g, float b,
                                float alpha, float lineWidth) {
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        double x1 = box.minX - camPos.x, x2 = box.maxX - camPos.x;
        double y1 = box.minY - camPos.y, y2 = box.maxY - camPos.y;
        double z1 = box.minZ - camPos.z, z2 = box.maxZ - camPos.z;

        double lx = (x2 - x1) * 0.25;
        double ly = (y2 - y1) * 0.25;
        double lz = (z2 - z1) * 0.25;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(lineWidth);

        matrices.push();
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        MatrixStack.Entry entry = matrices.peek();

        emitCorner(buf, entry, x1, y1, z1,  lx,  0,   0,  r, g, b, alpha);
        emitCorner(buf, entry, x1, y1, z1,  0,   ly,  0,  r, g, b, alpha);
        emitCorner(buf, entry, x1, y1, z1,  0,   0,   lz, r, g, b, alpha);

        emitCorner(buf, entry, x2, y1, z1, -lx,  0,   0,  r, g, b, alpha);
        emitCorner(buf, entry, x2, y1, z1,  0,   ly,  0,  r, g, b, alpha);
        emitCorner(buf, entry, x2, y1, z1,  0,   0,   lz, r, g, b, alpha);

        emitCorner(buf, entry, x1, y2, z1,  lx,  0,   0,  r, g, b, alpha);
        emitCorner(buf, entry, x1, y2, z1,  0,  -ly,  0,  r, g, b, alpha);
        emitCorner(buf, entry, x1, y2, z1,  0,   0,   lz, r, g, b, alpha);

        emitCorner(buf, entry, x2, y2, z1, -lx,  0,   0,  r, g, b, alpha);
        emitCorner(buf, entry, x2, y2, z1,  0,  -ly,  0,  r, g, b, alpha);
        emitCorner(buf, entry, x2, y2, z1,  0,   0,   lz, r, g, b, alpha);

        emitCorner(buf, entry, x1, y1, z2,  lx,  0,   0,  r, g, b, alpha);
        emitCorner(buf, entry, x1, y1, z2,  0,   ly,  0,  r, g, b, alpha);
        emitCorner(buf, entry, x1, y1, z2,  0,   0,  -lz, r, g, b, alpha);

        emitCorner(buf, entry, x2, y1, z2, -lx,  0,   0,  r, g, b, alpha);
        emitCorner(buf, entry, x2, y1, z2,  0,   ly,  0,  r, g, b, alpha);
        emitCorner(buf, entry, x2, y1, z2,  0,   0,  -lz, r, g, b, alpha);

        emitCorner(buf, entry, x1, y2, z2,  lx,  0,   0,  r, g, b, alpha);
        emitCorner(buf, entry, x1, y2, z2,  0,  -ly,  0,  r, g, b, alpha);
        emitCorner(buf, entry, x1, y2, z2,  0,   0,  -lz, r, g, b, alpha);

        emitCorner(buf, entry, x2, y2, z2, -lx,  0,   0,  r, g, b, alpha);
        emitCorner(buf, entry, x2, y2, z2,  0,  -ly,  0,  r, g, b, alpha);
        emitCorner(buf, entry, x2, y2, z2,  0,   0,  -lz, r, g, b, alpha);

        BufferRenderer.drawWithGlobalProgram(buf.end());
        matrices.pop();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);
    }

    private void emitCorner(BufferBuilder buf, MatrixStack.Entry entry,
                             double ox, double oy, double oz,
                             double dx, double dy, double dz,
                             float r, float g, float b, float a) {
        float len = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
        if (len == 0) return;
        float nx = (float)(dx / len), ny = (float)(dy / len), nz = (float)(dz / len);
        buf.vertex(entry, (float)ox, (float)oy, (float)oz).color(r,g,b,a).normal(entry,nx,ny,nz);
        buf.vertex(entry, (float)(ox+dx), (float)(oy+dy), (float)(oz+dz)).color(r,g,b,a).normal(entry,nx,ny,nz);
    }
}
