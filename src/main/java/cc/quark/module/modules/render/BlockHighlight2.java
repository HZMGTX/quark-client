package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class BlockHighlight2 extends Module {

    private final ColorSetting color     = register(new ColorSetting("Color", "Highlight color", 0xFF00FFFF));
    private final DoubleSetting lineWidth = register(new DoubleSetting("LineWidth", "Outline line thickness", 2.0, 0.5, 5.0));
    private final BoolSetting filled     = register(new BoolSetting("Filled", "Fill the block highlight", false));

    public BlockHighlight2() {
        super("BlockHighlight2", "Enhanced colored block highlight with adjustable line width around the targeted block", Category.RENDER);
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
                .expand(0.003);

        MatrixStack matrices = event.getMatrixStack();
        float r = color.getRedF();
        float g = color.getGreenF();
        float b = color.getBlueF();
        float a = color.getAlphaF();

        RenderUtil.drawESPBox(matrices, box, r, g, b, a, (float) lineWidth.get());

        if (filled.isEnabled()) {
            RenderUtil.drawFilledBox(matrices, box, r, g, b, a * 0.25f);
        }
    }
}
