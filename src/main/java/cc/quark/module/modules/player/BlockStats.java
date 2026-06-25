package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.client.gui.DrawContext;

public class BlockStats extends Module {

    private int blocksPlaced = 0;
    private int blocksBroken = 0;
    private boolean prevMining = false;
    private boolean prevPlacing = false;

    public BlockStats() {
        super("BlockStats", "Shows blocks placed/broken stats on HUD", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        blocksPlaced = 0;
        blocksBroken = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean isMining = mc.interactionManager != null && mc.interactionManager.isBreakingBlock();
        if (prevMining && !isMining) {
            blocksBroken++;
        }
        prevMining = isMining;

        boolean isPlacing = mc.options.useKey.isPressed();
        if (!prevPlacing && isPlacing) {
            blocksPlaced++;
        }
        prevPlacing = isPlacing;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        ctx.drawTextWithShadow(mc.textRenderer, "Placed: " + blocksPlaced, 4, 4, 0xFFAAFF55);
        ctx.drawTextWithShadow(mc.textRenderer, "Broken: " + blocksBroken, 4, 14, 0xFFFF5555);
    }
}
