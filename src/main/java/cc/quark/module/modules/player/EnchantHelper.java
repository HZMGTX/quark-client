package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;

public class EnchantHelper extends Module {
    private final BoolSetting warnUnenchanted = register(new BoolSetting("WarnUnenchanted", "Warn when equipping unenchanted gear", true));
    public EnchantHelper() { super("EnchantHelper", "Helps manage and track equipment enchantments", Category.PLAYER); }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || !warnUnenchanted.getValue()) return;
        ItemStack helm = mc.player.getInventory().getArmorStack(3);
        if (!helm.isEmpty() && mc.player.getWorld() != null) {
            RegistryWrapper.WrapperLookup wl = mc.player.getWorld().getRegistryManager();
        }
    }
}
