package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;

public class BlockHighlight3 extends Module {
    private final ColorSetting color = register(new ColorSetting("Color", "Highlight color", 0xFF00AAFF));
    private final DoubleSetting lineWidth = register(new DoubleSetting("Width", "Line width", 2.0, 0.5, 5.0));

    public BlockHighlight3() { super("BlockHighlight3", "Custom block selection outline", Category.RENDER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onRender3D(EventRender3D e) {
        if (!(mc.crosshairTarget instanceof BlockHitResult bhr)) return;
        MatrixStack ms = e.getMatrixStack();
        BlockPos pos = bhr.getBlockPos();
        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX()+1, pos.getY()+1, pos.getZ()+1);
        RenderUtil.drawESPBox(ms, box,
            color.getRedF(), color.getGreenF(), color.getBlueF(), color.getAlphaF(),
            (float) lineWidth.get());
    }
}
