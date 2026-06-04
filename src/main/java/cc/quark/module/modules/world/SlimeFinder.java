package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SlimeFinder extends Module {

    private final IntSetting  radius  = register(new IntSetting ("Radius",   "Chunk scan radius",        8, 1, 32));
    private final BoolSetting showHUD = register(new BoolSetting("Show HUD", "Show slime chunk count on HUD", true));

    private final List<long[]> slimeChunks = new ArrayList<>();

    public SlimeFinder() {
        super("SlimeFinder", "Highlights slime chunks near your current position", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        slimeChunks.clear();

        int seed = (int)(mc.world.getSeed() & 0xFFFFFFFFL);
        int cx = mc.player.getChunkPos().x;
        int cz = mc.player.getChunkPos().z;
        int r = radius.get();

        for (int x = cx - r; x <= cx + r; x++) {
            for (int z = cz - r; z <= cz + r; z++) {
                if (isSlimeChunk(seed, x, z)) {
                    slimeChunks.add(new long[]{x, z});
                }
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showHUD.isEnabled() || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int cx = mc.player.getChunkPos().x;
        int cz = mc.player.getChunkPos().z;
        boolean inSlime = slimeChunks.stream().anyMatch(c -> c[0] == cx && c[1] == cz);
        String text = "Slime Chunks: " + slimeChunks.size() + (inSlime ? " §a[IN SLIME]" : "");
        ctx.drawTextWithShadow(mc.textRenderer, text, 4, 220, inSlime ? 0xFF55FF55 : 0xFFFFFFFF);
    }

    private boolean isSlimeChunk(long seed, int x, int z) {
        Random rnd = new Random(seed +
                (long)(x * x * 0x4c1906) +
                (long)(x * 0x5ac0db) +
                (long)(z * z) * 0x4307a7L +
                (long)(z * 0x5f24f) ^ 0x3ad8025f);
        return rnd.nextInt(10) == 0;
    }
}
