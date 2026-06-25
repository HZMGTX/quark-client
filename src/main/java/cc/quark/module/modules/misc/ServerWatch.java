package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;

public class ServerWatch extends Module {

    private final BoolSetting showTPS   = register(new BoolSetting("ShowTPS",  "Show estimated server TPS",  true));
    private final BoolSetting showMSPT  = register(new BoolSetting("ShowMSPT", "Show estimated MSPT",        true));
    private final IntSetting  posX      = register(new IntSetting("X", "HUD X position", 4,   0, 500));
    private final IntSetting  posY      = register(new IntSetting("Y", "HUD Y position", 80,  0, 500));

    private long lastBlockUpdate = System.currentTimeMillis();
    private long intervalSum     = 0;
    private int  intervalCount   = 0;
    private double estimatedTPS  = 20.0;

    public ServerWatch() {
        super("ServerWatch", "Displays server TPS and MSPT estimates based on block update timing", Category.MISC);
    }

    @Override
    public void onEnable() {
        lastBlockUpdate = System.currentTimeMillis();
        intervalSum     = 0;
        intervalCount   = 0;
        estimatedTPS    = 20.0;
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof BlockUpdateS2CPacket) &&
                !(event.getPacket() instanceof ChunkDeltaUpdateS2CPacket)) return;

        long now = System.currentTimeMillis();
        long interval = now - lastBlockUpdate;
        lastBlockUpdate = now;

        if (interval > 0 && interval < 2000) {
            intervalSum += interval;
            intervalCount++;
            if (intervalCount >= 20) {
                double avgMs = (double) intervalSum / intervalCount;
                estimatedTPS = Math.min(20.0, 1000.0 / Math.max(avgMs, 50.0));
                intervalSum  = 0;
                intervalCount = 0;
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int x = posX.get();
        int y = posY.get();
        int lh = mc.textRenderer.fontHeight + 2;

        if (showTPS.isEnabled()) {
            String tpsColor = estimatedTPS >= 18 ? "§a" : estimatedTPS >= 12 ? "§e" : "§c";
            ctx.drawTextWithShadow(mc.textRenderer,
                    "§7TPS: " + tpsColor + String.format("%.1f", estimatedTPS), x, y, 0xFFFFFFFF);
            y += lh;
        }
        if (showMSPT.isEnabled()) {
            double mspt = estimatedTPS > 0 ? 1000.0 / estimatedTPS : 50.0;
            String msptColor = mspt <= 50 ? "§a" : mspt <= 100 ? "§e" : "§c";
            ctx.drawTextWithShadow(mc.textRenderer,
                    "§7MSPT: " + msptColor + String.format("%.1f", mspt) + "ms", x, y, 0xFFFFFFFF);
        }
    }
}
