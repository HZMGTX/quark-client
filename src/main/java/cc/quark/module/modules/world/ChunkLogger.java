package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.HashSet;
import java.util.Set;

public class ChunkLogger extends Module {
    private final StringSetting searchBlock = register(new StringSetting("Block", "Block to search for", "minecraft:diamond_ore"));
    private final BoolSetting alertInChat = register(new BoolSetting("Alert", "Show chunk coordinates in chat", true));
    private final TimerUtil timer = new TimerUtil();
    private final Set<ChunkPos> logged = new HashSet<>();

    public ChunkLogger() {
        super("Chunk Logger", "Logs chunks containing valuable blocks", Category.WORLD, 0);
    }

    @Override
    public void onEnable() { logged.clear(); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(5000)) return;
        timer.reset();

        BlockPos playerPos = mc.player.getBlockPos();
        ChunkPos playerChunk = new ChunkPos(playerPos);

        for (int cx = playerChunk.x - 4; cx <= playerChunk.x + 4; cx++) {
            for (int cz = playerChunk.z - 4; cz <= playerChunk.z + 4; cz++) {
                ChunkPos cp = new ChunkPos(cx, cz);
                if (logged.contains(cp)) continue;
                // Basic detection: check if any diamond ore in chunk
                outer:
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = -64; y < 20; y++) {
                            BlockPos bp = new BlockPos(cx * 16 + x, y, cz * 16 + z);
                            var block = mc.world.getBlockState(bp).getBlock();
                            if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
                                logged.add(cp);
                                if (alertInChat.isEnabled()) {
                                    ChatUtil.info("[ChunkLogger] Diamond chunk: " + cx + "," + cz);
                                }
                                break outer;
                            }
                        }
                    }
                }
            }
        }
    }
}
