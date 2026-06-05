package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class SessionInfo extends Module {
    private final BoolSetting showTime = register(new BoolSetting("Time", "Show session time", true));
    private final BoolSetting showKills = register(new BoolSetting("Kills", "Show estimated kills", false));
    private final IntSetting x = register(new IntSetting("X", "X position", 2, 0, 1000));
    private final IntSetting y = register(new IntSetting("Y", "Y position", 90, 0, 600));
    private long sessionStart = 0;

    public SessionInfo() { super("SessionInfo", "Shows session statistics on HUD", Category.MISC); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); sessionStart = System.currentTimeMillis(); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (mc.player == null) return;
        DrawContext ctx = e.getDrawContext();
        long elapsed = (System.currentTimeMillis() - sessionStart) / 1000;
        int py = y.get();
        if (showTime.isEnabled()) {
            String time = String.format("%02d:%02d:%02d", elapsed/3600, (elapsed%3600)/60, elapsed%60);
            cc.quark.util.RenderUtil.drawCustomText(ctx, "Session: " + time, x.get(), py, 0xFFAAAAAA);
            py += 10;
        }
    }
}
