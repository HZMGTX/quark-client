package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Hand;

public class AutoArmorRepair extends Module {

    private final IntSetting minDurability = register(new IntSetting("Min Durability", "Durability % to trigger repair", 20, 1, 100));
    private final BoolSetting useXp = register(new BoolSetting("Use XP", "Right-click XP bottle to trigger mending", true));
    private final BoolSetting warnOnly = register(new BoolSetting("Warn Only", "Only send warning, skip XP use", false));

    private int cooldown = 0;

    public AutoArmorRepair() {
        super("AutoArmorRepair", "Auto-repairs armor with mending using XP bottles", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        cooldown = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        boolean needsRepair = false;
        for (ItemStack stack : mc.player.getArmorItems()) {
            if (stack.isEmpty()) continue;
            if (!hasMending(stack)) continue;
            int maxDur = stack.getMaxDamage();
            if (maxDur <= 0) continue;
            int damage = stack.getDamage();
            int durPct = (int)(((double)(maxDur - damage) / maxDur) * 100.0);
            if (durPct < minDurability.get()) {
                needsRepair = true;
                break;
            }
        }

        if (!needsRepair) return;

        if (warnOnly.isEnabled()) {
            ChatUtil.warn("Armor durability is low! Consider repairing.");
            cooldown = 60;
            return;
        }

        if (!useXp.isEnabled()) return;

        int xpSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.EXPERIENCE_BOTTLE)) {
                xpSlot = i;
                break;
            }
        }
        if (xpSlot == -1) return;

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = xpSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prev;
        cooldown = 20;
    }

    private boolean hasMending(ItemStack stack) {
        if (mc.world == null) return false;
        var registry = mc.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        var mendingEntry = registry.getEntry(Enchantments.MENDING);
        return mendingEntry.isPresent() && EnchantmentHelper.getLevel(mendingEntry.get(), stack) > 0;
    }
}
