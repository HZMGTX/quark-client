package cc.quark.module.modules.misc;

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

public class CPSDisplay extends Module {

    private final IntSetting posX = register(new IntSetting(
            "X", "HUD X position", 4, 0, 500));
    private final IntSetting posY = register(new IntSetting(
            "Y", "HUD Y position", 24, 0, 500));

    private final List<Long> clickTimestamps = new ArrayList<>();

    public CPSDisplay() {
        super("CPSDisplay", "Displays clicks per second (CPS) as a HUD overlay", Category.MISC);
    }

    @Override
    public void onEnable() {
        clickTimestamps.clear();
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        clickTimestamps.add(System.currentTimeMillis());
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        long now = System.currentTimeMillis();
        // Remove clicks older than 1 second
        Iterator<Long> it = clickTimestamps.iterator();
        while (it.hasNext()) {
            if (now - it.next() > 1000L) it.remove();
        }

        int cps = clickTimestamps.size();
        String text = "CPS: " + cps;
        DrawContext ctx = event.getDrawContext();
        ctx.drawTextWithShadow(mc.textRenderer, text, posX.get(), posY.get(), 0xFFFFFFFF);
    }
}
