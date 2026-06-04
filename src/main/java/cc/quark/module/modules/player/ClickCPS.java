package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClickCPS extends Module {

    private final IntSetting x = register(new IntSetting(
            "X", "HUD X position", 4, 0, 3000));

    private final IntSetting y = register(new IntSetting(
            "Y", "HUD Y position", 180, 0, 3000));

    private final List<Long> clicks = new ArrayList<>();

    public ClickCPS() {
        super("ClickCPS", "Monitors and displays clicks per second", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        clicks.clear();
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        clicks.add(System.currentTimeMillis());
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        long now = System.currentTimeMillis();
        Iterator<Long> it = clicks.iterator();
        while (it.hasNext()) {
            if (now - it.next() > 1000L) it.remove();
        }

        DrawContext ctx = event.getDrawContext();
        String text = "CPS: " + clicks.size();
        ctx.drawTextWithShadow(mc.textRenderer, text, x.get(), y.get(), 0xFFFFFFFF);
    }
}
