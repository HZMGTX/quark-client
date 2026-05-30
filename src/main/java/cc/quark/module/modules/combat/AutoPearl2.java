package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * AutoPearl2 — throws an ender pearl when HP drops below a threshold,
 * with a configurable cooldown to prevent spam.
 */
public class AutoPearl2 extends Module {

    private final IntSetting hpThreshold = register(new IntSetting("HPThreshold", "Health at or below which to pearl",  6,    1,   20));
    private final IntSetting cooldown    = register(new IntSetting("Cooldown",    "Cooldown in ms between pearl throws", 3000, 500, 10000));

    private final TimerUtil timer    = new TimerUtil();
    private int             prevSlot = -1;

    public AutoPearl2() {
        super("AutoPearl2", "Automatically throws an ender pearl when HP is low", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.getHealth() > hpThreshold.get()) {
            restoreSlot();
            return;
        }
        if (!timer.hasReached(cooldown.get())) return;

        int pearlSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.ENDER_PEARL)) {
                pearlSlot = i;
                break;
            }
        }
        if (pearlSlot == -1) return;

        int cur = mc.player.getInventory().selectedSlot;
        if (cur != pearlSlot) {
            prevSlot = cur;
            mc.player.getInventory().selectedSlot = pearlSlot;
        }

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
        restoreSlot();
    }

    private void restoreSlot() {
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }
}
