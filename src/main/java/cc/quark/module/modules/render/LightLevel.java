package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.joml.Matrix4f;

/**
 * LightLevel — shows light levels on nearby blocks to help prevent mob spawning.
 * Displays colored tiles or numeric labels at unsafe spawn positions.
 *
 * Modes:
 *  - Tiles: Flat colored quad on the floor (red/yellow/green)
 *  - Numbers: 2D light-level number rendered on block surfaces
 *  - Both: Tiles and numbers together
 */
public class LightLevel extends Module {

    private final IntSetting   range     = register(new IntSetting(  "Range",     "Block scan radius",                   16, 4,  32));
    private final IntSetting   threshold = register(new IntSetting(  "Threshold", "Light level considered unsafe",         8, 1,  15));
    private final ModeSetting  mode      = register(new ModeSetting( "Mode",      "Render mode",       "Tiles", "Tiles", "Numbers", "Both"));
    private final ColorSetting dangerous = register(new ColorSetting("Dangerous", "Color for very unsafe (0–thresh/2)",  0xAAFF2222));
    private final ColorSetting warning   = register(new ColorSetting("Warning",   "Color for borderline (thresh/2–thresh)",0xAAFFCC00));
    private final BoolSetting  onlySolid = register(new BoolSetting( "Solid Only","Only show on solid-top blocks",         true));

    public LightLevel() {
        super("LightLevel", "Highlights block light levels to identify mob-spawn-safe zones", Category.RENDER);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        if (mode.is("Numbers")) return; // numbers drawn in 2D pass

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        BlockPos playerPos = mc.player.getBlockPos();
        int r = range.get();
        int thresh = threshold.get();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        MatrixStack matrices = event.getMatrixStack();
        matrices.push();
        Matrix4f mat = matrices.peek().getPositionMatrix();
        Tessellator tess = Tessellator.getInstance();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -4; dy <= 4; dy++) {
                    BlockPos pos = playerPos.add(dx, dy, dz);
                    BlockState state = mc.world.getBlockState(pos);
                    if (!state.isAir()) continue;

                    BlockPos below = pos.down();
                    BlockState belowState = mc.world.getBlockState(below);
                    if (onlySolid.isEnabled() && !belowState.isSolidBlock(mc.world, below)) continue;

                    int light = mc.world.getLightLevel(LightType.BLOCK, pos);
                    if (light >= thresh) continue;

                    int colorArgb = light <= thresh / 2 ? dangerous.get() : warning.get();
                    float fr = ((colorArgb >> 16) & 0xFF) / 255f;
                    float fg = ((colorArgb >> 8)  & 0xFF) / 255f;
                    float fb = (colorArgb         & 0xFF) / 255f;
                    float fa = ((colorArgb >> 24) & 0xFF) / 255f;

                    double x1 = pos.getX() - camPos.x + 0.01;
                    double y1 = pos.getY() - camPos.y + 0.005;
                    double z1 = pos.getZ() - camPos.z + 0.01;
                    double x2 = x1 + 0.98;
                    double z2 = z1 + 0.98;

                    BufferBuilder buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                    buf.vertex(mat, (float)x1, (float)y1, (float)z1).color(fr, fg, fb, fa);
                    buf.vertex(mat, (float)x2, (float)y1, (float)z1).color(fr, fg, fb, fa);
                    buf.vertex(mat, (float)x2, (float)y1, (float)z2).color(fr, fg, fb, fa);
                    buf.vertex(mat, (float)x1, (float)y1, (float)z2).color(fr, fg, fb, fa);
                    BufferRenderer.drawWithGlobalProgram(buf.end());
                }
            }
        }

        matrices.pop();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;
        if (mode.is("Tiles")) return; // tiles only

        // Show the light level under the player's feet as a quick HUD readout
        BlockPos pos = mc.player.getBlockPos();
        int light = mc.world.getLightLevel(LightType.BLOCK, pos);
        int thresh = threshold.get();

        int textColor;
        if (light <= thresh / 2) {
            textColor = 0xFFFF4444;
        } else if (light < thresh) {
            textColor = 0xFFFFCC00;
        } else {
            textColor = 0xFF44FF44;
        }

        DrawContext ctx = event.getDrawContext();
        String label = "Light: " + light;
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        ctx.drawTextWithShadow(mc.textRenderer, label, sw / 2 - mc.textRenderer.getWidth(label) / 2, sh - 60, textColor);
    }
}
