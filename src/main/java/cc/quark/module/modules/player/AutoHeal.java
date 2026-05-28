package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AutoHeal extends Module {

    private final DoubleSetting health = register(new DoubleSetting("Health", "Heal below this health (hearts)", 8.0, 1.0, 15.0));
    private final TimerUtil timer = new TimerUtil();
    private int savedSlot = -1;

    public AutoHeal() {
        super("AutoHeal", "Auto-uses healing items when health is low", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!timer.hasReached(500)) return;
        if (mc.player.getHealth() > (float) (health.get() * 2)) return;

        int slot = findHealItem();
        if (slot == -1) return;

        savedSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        timer.reset();
    }

    private int findHealItem() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) return i;
        }
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.GOLDEN_APPLE) return i;
        }
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) return i;
        }
        return -1;
    }

    @Override
    public void onDisable() {
        if (mc.player != null && savedSlot != -1) {
            mc.player.getInventory().selectedSlot = savedSlot;
            savedSlot = -1;
        }
    }
}
