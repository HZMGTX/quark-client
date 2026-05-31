package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.ChatUtil;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class SpawnPoint extends Module {

    public SpawnPoint() {
        super("SpawnPoint", "Shows current spawnpoint; press END to set spawn at current position", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        BlockPos spawn = mc.player.getSpawnPointPosition();
        if (spawn != null) {
            ChatUtil.info("[SpawnPoint] Current spawn: " + spawn.getX() + ", " + spawn.getY() + ", " + spawn.getZ());
        } else {
            ChatUtil.info("[SpawnPoint] No custom spawn set (world spawn)");
        }
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (event.getKeyCode() != GLFW.GLFW_KEY_END) return;
        if (mc.player == null) return;

        BlockPos pos = mc.player.getBlockPos();
        ChatUtil.info("[SpawnPoint] Attempting to set spawn at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        ChatUtil.warn("[SpawnPoint] Sleep in a bed at this location to confirm spawn");
    }
}
