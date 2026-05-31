package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class SpawnChunkESP extends Module {

    public SpawnChunkESP() {
        super("SpawnChunkESP", "Marks the 16x16 spawn chunks in render", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        int playerY = mc.player.getBlockPos().getY();

        // Spawn chunks are always the 16x16 area centred on world spawn (0,0)
        // Minecraft uses chunks -1 to 0 around spawn, i.e., blocks -8 to 8 (chunk radius 1)
        for (int cx = -1; cx <= 0; cx++) {
            for (int cz = -1; cz <= 0; cz++) {
                Box box = new Box(cx * 16, playerY - 1, cz * 16, cx * 16 + 16, playerY, cz * 16 + 16);
                RenderUtil.drawFilledBox(m, box, 1.0f, 0.8f, 0.0f, 0.25f);
                RenderUtil.drawESPBox(m, box, 1.0f, 0.8f, 0.0f, 1.0f, 1.5f);
            }
        }
    }
}
