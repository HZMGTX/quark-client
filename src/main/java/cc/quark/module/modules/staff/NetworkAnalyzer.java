package cc.quark.module.modules.staff;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.setting.BoolSetting;
import cc.quark.module.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.packet.Packet;

import java.util.*;

public class NetworkAnalyzer extends Module {

    private final IntSetting maxEntries = new IntSetting("MaxEntries", 10, 5, 30);
    private final BoolSetting showSize  = new BoolSetting("ShowSize", false);
    private final IntSetting posX       = new IntSetting("X", 4, 0, 800);
    private final IntSetting posY       = new IntSetting("Y", 200, 0, 800);

    private final Map<String, Integer> packetCounts = new LinkedHashMap<>();
    private int totalPackets = 0;

    public NetworkAnalyzer() {
        super("NetworkAnalyzer", "Displays incoming packet types and frequency in a HUD overlay", Category.STAFF);
        addSettings(maxEntries, showSize, posX, posY);
    }

    @Override public void onEnable()  { Quark.mc.getEventBus().subscribe(this); packetCounts.clear(); totalPackets = 0; }
    @Override public void onDisable() { Quark.mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onPacket(EventPacketReceive event) {
        String name = event.getPacket().getClass().getSimpleName();
        packetCounts.merge(name, 1, Integer::sum);
        totalPackets++;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        var mc = Quark.mc;
        if (mc == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int x = posX.get(), y = posY.get();

        ctx.drawTextWithShadow(mc.textRenderer, "§b[NetAnalyzer] §7total:" + totalPackets, x, y, 0xFFFFFFFF);
        y += 11;

        packetCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(maxEntries.get())
            .forEach(e -> {
                ctx.drawTextWithShadow(mc.textRenderer,
                    "§7" + e.getKey().replace("S2CPacket","").replace("Packet","") + " §f" + e.getValue(),
                    x, y + packetCounts.size(), 0xFFCCCCCC);
            });
    }
}
