package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
//? if mc >= "1.20.5" {
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import java.util.Map;
//?} else {
/*import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import java.util.Map;*/
//?}
import net.minecraft.item.ItemStack;

public class WeaponInfo extends Module {

    private final IntSetting posX = register(new IntSetting("X", "HUD X position", 4, 0, 500));
    private final IntSetting posY = register(new IntSetting("Y", "HUD Y position", 80, 0, 500));

    public WeaponInfo() {
        super("WeaponInfo", "Shows held weapon name, damage, durability, and enchantments", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        ItemStack stack = mc.player.getMainHandStack();
        if (stack.isEmpty()) return;

        DrawContext ctx = event.getDrawContext();
        int x = posX.get();
        int y = posY.get();
        int lineH = mc.textRenderer.fontHeight + 2;

        // Item name
        String name = stack.getName().getString();
        ctx.drawTextWithShadow(mc.textRenderer, name, x, y, 0xFFFFFFFF);
        y += lineH;

        // Durability
        if (stack.isDamageable()) {
            int maxDmg = stack.getMaxDamage();
            int remaining = maxDmg - stack.getDamage();
            float pct = maxDmg > 0 ? (float) remaining / maxDmg : 1f;
            int durColor = pct > 0.5f ? 0xFF55FF55 : pct > 0.25f ? 0xFFFFFF55 : 0xFFFF5555;
            String durText = "Durability: " + remaining + "/" + maxDmg;
            ctx.drawTextWithShadow(mc.textRenderer, durText, x, y, durColor);
            y += lineH;
        }

        // Attack damage (approximate via component)
        ctx.drawTextWithShadow(mc.textRenderer, "Item: " + stack.getItem().getClass().getSimpleName()
                .replace("Item", ""), x, y, 0xFFAAAAAA);
        y += lineH;

        // Enchantments
        //? if mc >= "1.20.5" {
        ItemEnchantmentsComponent enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchantments != null && !enchantments.isEmpty()) {
            for (Map.Entry<RegistryEntry<Enchantment>, Integer> entry : enchantments.getEnchantmentEntries()) {
                String enchName = entry.getKey().getIdAsString();
                if (enchName.contains(":")) enchName = enchName.split(":")[1];
                enchName = enchName.replace("_", " ");
                int lvl = entry.getValue();
                String label = capitalize(enchName) + (lvl > 1 ? " " + lvl : "");
                ctx.drawTextWithShadow(mc.textRenderer, label, x, y, 0xFFFFAA00);
                y += lineH;
            }
        }
        //?} else {
        /*Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            String enchName = entry.getKey().getName(entry.getValue()).getString().replace("_", " ");
            int lvl = entry.getValue();
            String label = capitalize(enchName) + (lvl > 1 ? " " + lvl : "");
            ctx.drawTextWithShadow(mc.textRenderer, label, x, y, 0xFFFFAA00);
            y += lineH;
        }*/
        //?}
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] parts = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                if (!sb.isEmpty()) sb.append(" ");
                sb.append(Character.toUpperCase(p.charAt(0)));
                if (p.length() > 1) sb.append(p.substring(1));
            }
        }
        return sb.toString();
    }
}
