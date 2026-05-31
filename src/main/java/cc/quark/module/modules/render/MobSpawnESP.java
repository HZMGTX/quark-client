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

public class MobSpawnESP extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Search radius in blocks", 16, 4, 32));

    public MobSpawnESP() {
        super("MobSpawnESP", "Highlights blocks where mobs can spawn (light level 0) in red", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        BlockPos playerPos = mc.player.getBlockPos();
        int r = range.get();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (!mc.world.getBlockState(pos).isAir()) continue;
                    if (!mc.world.getBlockState(pos.down()).isSolidBlock(mc.world, pos.down())) continue;
                    if (mc.world.getLightLevel(pos) > 0) continue;
                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(),
                            pos.getX() + 1, pos.getY() + 0.05, pos.getZ() + 1);
                    RenderUtil.drawFilledBox(m, box, 1f, 0f, 0f, 0.4f);
                    RenderUtil.drawESPBox(m, box, 1f, 0f, 0f, 0.8f, 1.0f);
                }
            }
        }
    }
}
