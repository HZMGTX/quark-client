package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class BlockHighlight extends Module {

    private final ColorSetting color = register(new ColorSetting(
            "Color", "Outline color", 0xFFFFFFFF));
    private final DoubleSetting lineWidth = register(new DoubleSetting(
            "Line Width", "Outline thickness", 2.0, 0.5, 5.0));
    private final DoubleSetting expand = register(new DoubleSetting(
            "Expand", "Box expansion", 0.002, 0.0, 0.05));

    public BlockHighlight() {
        super("BlockHighlight", "Draws a colored outline around the block you are looking at", Category.RENDER);
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
        float r = color.getRedF(), g = color.getGreenF(), b = color.getBlueF();
        RenderUtil.drawESPBox(matrices, box, r, g, b, 0.9f, (float) lineWidth.get());
    }
}
