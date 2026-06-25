package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.util.Hand;

public class AntiAFKfish extends Module {
    private final IntSetting interval = register(new IntSetting("Interval", "Ticks between reel/recast", 100, 20, 400));
    private final TimerUtil timer = new TimerUtil();

    public AntiAFKfish() {
        super("AFK Fish", "Auto reel and recast fishing rod to prevent AFK kick", Category.MISC, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!timer.hasReached(interval.get() * 50L)) return;
        if (mc.player.fishHook != null) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            timer.reset();
        }
    }
}
