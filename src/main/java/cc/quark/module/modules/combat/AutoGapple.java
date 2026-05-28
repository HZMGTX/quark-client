package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoGapple extends Module {

    private final DoubleSetting health = register(new DoubleSetting("Health", "Eat a gapple when health is at or below this value", 8.0, 1.0, 10.0));
    private final BoolSetting enchanted = register(new BoolSetting("Enchanted", "Prefer enchanted golden apples", true));

    private final TimerUtil timer = new TimerUtil();
    private int previousSlot = -1;
    private boolean eating = false;

    public AutoGapple() {
        super("AutoGapple", "Automatically eats golden apples when health is low", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        previousSlot = -1;
        eating = false;
        timer.reset();
    }

    @Override
    public void onDisable() {
        if (mc.player != null && previousSlot != -1) {
            mc.player.getInventory().selectedSlot = previousSlot;
        }
        previousSlot = -1;
        eating = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(500)) return;

        float currentHealth = mc.player.getHealth();
        float maxHealth = mc.player.getMaxHealth();
        float threshold = (float) health.get() * (maxHealth / 20f);

        if (eating && currentHealth >= threshold) {
            if (previousSlot != -1) {
                mc.player.getInventory().selectedSlot = previousSlot;
                previousSlot = -1;
            }
            eating = false;
            return;
        }

        if (currentHealth >= threshold) return;

        int slot = findGappleSlot();
        if (slot == -1) {
            if (eating && previousSlot != -1) {
                mc.player.getInventory().selectedSlot = previousSlot;
                previousSlot = -1;
            }
            eating = false;
            return;
        }

        if (mc.player.getInventory().selectedSlot != slot) {
            if (!eating) previousSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
        }

        eating = true;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        timer.reset();
    }

    private int findGappleSlot() {
        int regularSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) return i;
            if (stack.isOf(Items.GOLDEN_APPLE) && regularSlot == -1) {
                if (!enchanted.isEnabled()) return i;
                regularSlot = i;
            }
        }
        return regularSlot;
    }
}
