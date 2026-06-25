package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class PearlThrower extends Module {
    private final IntSetting delay = register(new IntSetting("Delay", "Delay between throws (ms)", 500, 100, 3000));
    private final IntSetting count = register(new IntSetting("Count", "Pearls to throw", 1, 1, 16));
    private final TimerUtil timer = new TimerUtil();
    private int thrown = 0;

    public PearlThrower() { super("PearlThrower", "Throws ender pearls rapidly on demand", Category.PLAYER); }
    @Override public void onEnable() { thrown = 0; }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (thrown >= count.get()) { disable(); return; }
        if (!timer.hasReached(delay.get())) return;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ENDER_PEARL) {
                int prev = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.getInventory().selectedSlot = prev;
                thrown++;
                timer.reset();
                return;
            }
        }
    }
}
