package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class BlockHighlight extends Module {

    private final ColorSetting  color     = register(new ColorSetting("Color", "Highlight color", 0xFF00AAFF));
    private final DoubleSetting lineWidth = register(new DoubleSetting("Line Width", "Outline thickness", 2.0, 0.5, 5.0));
    private final DoubleSetting expand    = register(new DoubleSetting("Expand", "Box expansion amount", 0.003, 0.0, 0.05));
    private final ModeSetting   mode      = register(new ModeSetting("Mode", "Render mode", "Outline", "Outline", "Fill", "Both"));

    public BlockHighlight() {
        super("BlockHighlight", "Highlights the targeted block with a colored outline, fill, or both", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.crosshairTarget == null) return;
        if (mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.world.getBlockState(pos);
        if (state.isAir()) return;

        Box box = state.getOutlineShape(mc.world, pos)
                       .getBoundingBox()
                       .offset(pos)
                       .expand(expand.get());

        MatrixStack matrices = event.getMatrixStack();
        float r = color.getRedF(), g = color.getGreenF(), b = color.getBlueF(), a = color.getAlphaF();

        String m = mode.get();
        if (m.equals("Outline") || m.equals("Both")) {
            RenderUtil.drawESPBox(matrices, box, r, g, b, Math.min(1f, a + 0.1f), (float) lineWidth.get());
        }
        if (m.equals("Fill") || m.equals("Both")) {
            RenderUtil.drawFilledBox(matrices, box, r, g, b, Math.max(0.05f, a * 0.3f));
        }
    }
}
