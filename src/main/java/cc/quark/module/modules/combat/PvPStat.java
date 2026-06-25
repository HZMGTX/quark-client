package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class PvPStat extends Module {
    private final IntSetting x = register(new IntSetting("X", "HUD X", 2, 0, 1920));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y", 100, 0, 1080));
    private final BoolSetting showOnHUD = register(new BoolSetting("Show HUD", "Show stats overlay", true));

    private int kills = 0;
    private int deaths = 0;

    public PvPStat() {
        super("PvP Stats", "Track kills and deaths this session", Category.COMBAT, 0);
    }

    @Override
    public void onEnable() { kills = 0; deaths = 0; }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming() || mc.player == null) return;
        String msg = event.getMessage();
        if (msg == null) return;
        String clean = msg.replaceAll("§[0-9a-fklmnorA-FK-OR]", "").toLowerCase();
        String myName = mc.player.getName().getString().toLowerCase();

        // Detect kill messages containing player name
        if (clean.contains(myName) && (clean.contains("killed") || clean.contains("slain") || clean.contains("defeated"))) {
            kills++;
        }
        if (clean.contains("you died") || clean.contains("you were slain")) {
            deaths++;
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showOnHUD.isEnabled() || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        ctx.drawText(mc.textRenderer, "§aK: §f" + kills + " §cD: §f" + deaths, x.get(), y.get(), 0xFFFFFF, true);
    }
}
