package cc.quark.module.modules.combat;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.LinkedHashMap;
import java.util.Map;

public class TotemPop extends Module {

    private final BoolSetting announce = register(new BoolSetting("Announce", "Send pop alerts to chat", true));
    private final BoolSetting trackSelf = register(new BoolSetting("Track Self", "Also track your own pops", true));

    private final Map<String, Integer> popCounts = new LinkedHashMap<>();

    public TotemPop() {
        super("TotemPop", "Tracks totem pops per player with HUD counter", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        popCounts.clear();
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.world == null || mc.player == null) return;
        if (!(event.getPacket() instanceof EntityStatusS2CPacket pkt)) return;
        if (pkt.getStatus() != 35) return;

        var entity = pkt.getEntity(mc.world);
        if (!(entity instanceof PlayerEntity player)) return;
        if (!trackSelf.isEnabled() && entity == mc.player) return;

        String name = player.getGameProfile().getName();
        int count = popCounts.getOrDefault(name, 0) + 1;
        popCounts.put(name, count);

        if (announce.isEnabled()) {
            ChatUtil.warn(name + " popped a totem! (" + count + "x)");
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (popCounts.isEmpty()) return;
        DrawContext ctx = event.getDrawContext();
        int y = 40;
        for (Map.Entry<String, Integer> entry : popCounts.entrySet()) {
            String text = entry.getKey() + ": " + entry.getValue() + " pops";
            ctx.drawTextWithShadow(mc.textRenderer, text, 4, y, 0xFFFF5555);
            y += 10;
        }
    }

    @Override
    public String getSuffix() {
        int total = popCounts.values().stream().mapToInt(Integer::intValue).sum();
        return total + " pops";
    }
}
