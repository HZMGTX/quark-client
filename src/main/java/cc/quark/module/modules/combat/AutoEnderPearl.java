package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * AutoEnderPearl — automatically selects the ender pearl slot when the player
 * is falling fast (velocity.y < -threshold). Optionally throws the pearl.
 */
public class AutoEnderPearl extends Module {

    private final DoubleSetting fallThreshold = register(new DoubleSetting("FallThreshold", "Downward velocity to trigger",           2.0, 0.5, 5.0));
    private final BoolSetting   autoThrow     = register(new BoolSetting  ("AutoThrow",     "Automatically throw the pearl",          false));
    private final IntSetting    hotbarSlot    = register(new IntSetting   ("HotbarSlot",    "Hotbar slot for pearl (-1 = auto-find)", -1,   -1,  8));

    private int prevSlot = -1;

    public AutoEnderPearl() {
        super("AutoEnderPearl", "Selects ender pearl when falling fast to prevent death", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        double vy = mc.player.getVelocity().y;
        if (vy >= -fallThreshold.get()) {
            restoreSlot();
            return;
        }

        int pearlSlot = findPearlSlot();
        if (pearlSlot == -1) return;

        int cur = mc.player.getInventory().selectedSlot;
        if (cur != pearlSlot) {
            prevSlot = cur;
            mc.player.getInventory().selectedSlot = pearlSlot;
        }

        if (autoThrow.isEnabled()) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
            restoreSlot();
        }
    }

    private int findPearlSlot() {
        int configured = hotbarSlot.get();
        if (configured >= 0 && configured <= 8) {
            ItemStack s = mc.player.getInventory().getStack(configured);
            if (s.isOf(Items.ENDER_PEARL)) return configured;
        }
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.ENDER_PEARL)) return i;
        }
        return -1;
    }

    private void restoreSlot() {
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }
}
