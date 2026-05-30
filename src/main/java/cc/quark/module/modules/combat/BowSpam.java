package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * BowSpam — rapidly charges and releases a bow or crossbow to maximize projectile fire rate.
 */
public class BowSpam extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks between charge-and-release cycles", 3, 1, 20));

    private final TimerUtil timer = new TimerUtil();
    private int tickCounter = 0;
    private boolean charging = false;

    public BowSpam() {
        super("BowSpam", "Rapidly charges and releases bows for maximum fire rate", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        tickCounter = 0;
        charging = false;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.options.useKey.setPressed(false);
        }
        charging = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        ItemStack held = mc.player.getMainHandStack();
        boolean hasBow = held.getItem() instanceof BowItem || held.getItem() instanceof CrossbowItem;

        if (!hasBow) {
            // Check offhand
            ItemStack offhand = mc.player.getOffHandStack();
            hasBow = offhand.getItem() instanceof BowItem || offhand.getItem() instanceof CrossbowItem;
        }

        if (!hasBow) {
            mc.options.useKey.setPressed(false);
            charging = false;
            return;
        }

        tickCounter++;

        if (!charging) {
            // Start charging
            mc.options.useKey.setPressed(true);
            charging = true;
        }

        // Release after delay ticks
        if (tickCounter >= delay.get()) {
            // Release and re-use to fire
            mc.options.useKey.setPressed(false);
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            tickCounter = 0;
            charging = false;
        }
    }
}
