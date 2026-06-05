package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemInfo extends Module {

    private final BoolSetting showDurability  = register(new BoolSetting("Durability",    "Show item durability",          true));
    private final BoolSetting showEnchants    = register(new BoolSetting("Enchantments",  "Show enchantments on held item", true));
    private final BoolSetting showCount       = register(new BoolSetting("Stack Count",   "Show item stack count",          true));
    private final BoolSetting showFood        = register(new BoolSetting("Food Value",    "Show food/saturation values",    true));
    private final ColorSetting textColor      = register(new ColorSetting("Color",        "HUD text color (ARGB)",          0xFFAAAAAA));
    private final IntSetting posX             = register(new IntSetting  ("X",            "HUD X position",                 4, 0, 500));
    private final IntSetting posY             = register(new IntSetting  ("Y",            "HUD Y position",                 60, 0, 500));

    public ItemInfo() {
        super("ItemInfo", "Shows detailed information about the held item in the HUD", Category.MISC);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        ItemStack held = mc.player.getMainHandStack();
        if (held == null || held.isEmpty()) return;

        DrawContext ctx  = event.getDrawContext();
        int x            = posX.get();
        int y            = posY.get();
        int lineHeight   = mc.textRenderer.fontHeight + 2;
        int col          = textColor.get();

        List<String> lines = new ArrayList<>();

        // Item name
        lines.add("§fItem: §r" + held.getName().getString());

        // Stack count
        if (showCount.isEnabled() && held.getCount() > 1) {
            lines.add("§7Count: §f" + held.getCount());
        }

        // Durability
        if (showDurability.isEnabled() && held.isDamageable()) {
            int maxDur  = held.getMaxDamage();
            int curDur  = maxDur - held.getDamage();
            float pct   = (float) curDur / maxDur * 100f;
            String durColor = pct > 60 ? "§a" : pct > 30 ? "§e" : "§c";
            lines.add("§7Durability: " + durColor + curDur + "§7/§f" + maxDur
                    + " §7(" + durColor + String.format("%.0f%%", pct) + "§7)");
        }

        // Enchantments
        if (showEnchants.isEnabled()) {
            Map<RegistryEntry<Enchantment>, Integer> enchants = EnchantmentHelper.getEnchantments(held).getEnchantments();
            for (Map.Entry<RegistryEntry<Enchantment>, Integer> entry : enchants.entrySet()) {
                String name = entry.getKey().value().description().getString();
                int level   = entry.getValue();
                lines.add("§9" + name + (level > 1 ? " " + toRoman(level) : ""));
            }
        }

        // Food / saturation
        if (showFood.isEnabled() && held.getItem().isFood()) {
            var foodComp = held.get(net.minecraft.component.DataComponentTypes.FOOD);
            if (foodComp != null) {
                lines.add("§7Food: §f+" + foodComp.nutrition()
                        + " §7Sat: §f+" + String.format("%.1f", foodComp.saturation()));
            }
        }

        // Draw all lines
        for (String line : lines) {
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal(line), x, y, col);
            y += lineHeight;
        }
    }

    private static String toRoman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(n);
        };
    }
}
