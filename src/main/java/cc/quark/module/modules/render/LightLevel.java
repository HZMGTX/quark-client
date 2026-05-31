package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

public class LightLevel extends Module {

    private final IntSetting threshold = register(new IntSetting("Threshold", "Light level at or below which spawning is possible", 7, 0, 15));
    private final IntSetting range     = register(new IntSetting("Range", "Block radius to scan", 16, 4, 32));

    public LightLevel() {
        super("LightLevel", "Renders light level numbers on nearby blocks; red=unsafe for spawning", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        BlockPos origin = mc.player.getBlockPos();
        int r = range.get();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                BlockPos groundPos = origin.add(dx, 0, dz);
                for (int dy = -4; dy <= 4; dy++) {
                    BlockPos check = groundPos.add(0, dy, 0);
                    if (!mc.world.getBlockState(check).isAir()) continue;
                    BlockPos below = check.down();
                    if (mc.world.getBlockState(below).isAir()) continue;

                    int light = mc.world.getLightLevel(LightType.BLOCK, check);
                    double[] screen = RenderUtil.project(new Vec3d(check.getX() + 0.5, check.getY() + 0.1, check.getZ() + 0.5));
                    if (screen == null) continue;

                    int color = light <= threshold.get() ? 0xFFFF3333 : 0xFF33FF33;
                    String text = String.valueOf(light);
                    int tw = mc.textRenderer.getWidth(text);
                    RenderUtil.drawCustomText(ctx, text, (int) screen[0] - tw / 2, (int) screen[1], color);
                    break;
                }
            }
        }
    }
}
