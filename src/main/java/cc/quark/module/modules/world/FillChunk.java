package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class FillChunk extends Module {

    private final StringSetting blockId = register(new StringSetting("Block", "Block type to fill with", "stone"));
    private final IntSetting height = register(new IntSetting("Height", "Fill height", 10, 1, 64));
    private final BoolSetting confirm = register(new BoolSetting("Confirm", "Enable to actually fill", false));

    public FillChunk() {
        super("FillChunk", "Fills the current chunk with specified block", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!confirm.getValue() || mc.player == null || mc.world == null) return;
        if (mc.interactionManager == null) return;

        ChunkPos cp = mc.player.getChunkPos();
        int startX = cp.getStartX(), startZ = cp.getStartZ();
        int baseY = (int) mc.player.getY();

        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                for (int y = baseY; y < baseY + height.getValue(); y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (mc.world.getBlockState(pos).isAir()) {
                        mc.interactionManager.attackBlock(pos, net.minecraft.util.math.Direction.UP);
                    }
                }
            }
        }
        confirm.setValue(false);
    }
}
