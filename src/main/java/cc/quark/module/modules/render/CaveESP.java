package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class CaveESP extends Module {

    private final IntSetting depth  = register(new IntSetting("Depth",  "How many blocks below player to scan", 20, 5, 64));
    private final IntSetting radius = register(new IntSetting("Radius", "Horizontal scan radius", 8, 2, 20));

    public CaveESP() {
        super("CaveESP", "Renders overlay for cave openings visible below the player's position", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        BlockPos playerPos = mc.player.getBlockPos();
        int r = radius.get();
        int d = depth.get();

        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                for (int y = 1; y <= d; y++) {
                    BlockPos pos = playerPos.add(x, -y, z);
                    if (!mc.world.getBlockState(pos).isAir()) continue;
                    BlockPos above = pos.up();
                    if (mc.world.getBlockState(above).isAir()) continue;
                    float shade = 1f - (float) y / d;
                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(),
                            pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
                    RenderUtil.drawFilledBox(m, box, 0f, shade * 0.6f, shade, 0.15f);
                }
            }
        }
    }
}
