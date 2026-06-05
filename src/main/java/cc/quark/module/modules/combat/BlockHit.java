package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.util.Hand;

public class BlockHit extends Module {
    private final IntSetting delay = register(new IntSetting("Delay", "Ticks between block", 3, 1, 10));
    private int timer = 0;
    private boolean blocking = false;

    public BlockHit() { super("BlockHit", "Auto right-click to block after attacking", Category.COMBAT); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.interactionManager == null) return;
        timer++;
        if (timer >= delay.get()) {
            if (!blocking) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                blocking = true;
            } else {
                blocking = false;
            }
            timer = 0;
        }
    }
}
