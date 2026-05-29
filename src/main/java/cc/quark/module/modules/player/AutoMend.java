package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.InventoryUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoMend extends Module {

    private final IntSetting threshold = register(new IntSetting(
            "DurabilityThreshold", "Percent durability remaining to trigger mending", 20, 1, 80));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;

    public AutoMend() {
        super("AutoMend", "Equips low-durability items and throws XP bottles to trigger Mending", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        if (prevSlot >= 0 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!timer.hasReached(600)) return;

        // Find a low-durability item in equipment slots
        ItemStack target = findLowDurabilityItem();
        if (target == null || target.isEmpty()) return;

        // Find XP bottle
        int xpSlot = InventoryUtil.findItem(Items.EXPERIENCE_BOTTLE);
        if (xpSlot < 0 || xpSlot >= 9) return;

        // Switch to XP bottle and throw it
        if (prevSlot < 0) prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = xpSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        timer.reset();
    }

    private ItemStack findLowDurabilityItem() {
        if (mc.player == null) return null;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = mc.player.getEquippedStack(slot);
            if (stack.isEmpty() || !stack.isDamageable()) continue;
            int pct = (int) (100.0 * (stack.getMaxDamage() - stack.getDamage()) / (double) stack.getMaxDamage());
            if (pct <= threshold.get()) return stack;
        }
        return null;
    }
}
