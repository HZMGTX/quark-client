package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.StringJoiner;

public class ItemShare extends Module {

    public ItemShare() {
        super("ItemShare", "Shares held item name, enchants, and durability to chat on key press", Category.MISC);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (event.getKeyCode() != GLFW.GLFW_KEY_HOME) return;
        if (mc.player == null) return;
        ItemStack stack = mc.player.getMainHandStack();
        if (stack.isEmpty()) {
            mc.player.sendMessage(net.minecraft.text.Text.literal("§7[ItemShare] §cHolding nothing."), false);
            return;
        }
        String name = stack.getName().getString();
        int durability = stack.getMaxDamage() > 0 ? stack.getMaxDamage() - stack.getDamage() : -1;
        StringBuilder sb = new StringBuilder("[" + name + "]");
        if (durability >= 0) {
            sb.append(" Dur:").append(durability).append("/").append(stack.getMaxDamage());
        }
        Map<RegistryEntry<Enchantment>, Integer> enchants = EnchantmentHelper.getEnchantments(stack).getEnchantments();
        if (!enchants.isEmpty()) {
            StringJoiner ej = new StringJoiner(", ");
            for (Map.Entry<RegistryEntry<Enchantment>, Integer> e : enchants.entrySet()) {
                String eName = e.getKey().value().description().getString();
                ej.add(eName + " " + e.getValue());
            }
            sb.append(" [").append(ej).append("]");
        }
        mc.player.networkHandler.sendChatMessage(sb.toString());
    }
}
