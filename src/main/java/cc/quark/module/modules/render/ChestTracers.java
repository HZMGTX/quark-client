package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ChestTracers extends Module {

    private final ColorSetting color = register(new ColorSetting("Color", "Tracer color", 0xFFFFAA00));
    private final IntSetting range = register(new IntSetting("Range", "Scan range", 32, 8, 64));

    public ChestTracers() {
        super("ChestTracers", "Draws tracer lines to nearby chests", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();
        Vec3d from = mc.player.getCameraPosVec(td);
        BlockPos origin = mc.player.getBlockPos();
        int r = range.get();
        for (BlockPos pos : BlockPos.iterate(origin.add(-r, -r, -r), origin.add(r, r, r))) {
            if (mc.world.getBlockEntity(pos) instanceof ChestBlockEntity) {
                RenderUtil.drawLine3D(m, from, Vec3d.ofCenter(pos),
                        color.getRedF(), color.getGreenF(), color.getBlueF(), color.getAlphaF(), 1.0f);
            }
        }
    }
}
