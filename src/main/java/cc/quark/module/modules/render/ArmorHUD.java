package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ColorUtil;
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

public class ArmorHUD extends Module {

    private final BoolSetting showEnchants = register(new BoolSetting("Enchants", "Show enchantment abbreviations on items", true));
    private final BoolSetting showDurability = register(new BoolSetting("Durability", "Show durability numbers and bar", true));

    public ArmorHUD() {
        super("ArmorHUD", "Shows armor pieces and durability in the corner of the screen", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int screenH = mc.getWindow().getScaledHeight();

        int x = 6;
        int slotH = 20;
        int totalSlots = 5;
        int startY = screenH - 22 - (totalSlots * slotH);

        int[] armorSlots = {39, 38, 37, 36};

        for (int i = 0; i < 4; i++) {
            ItemStack stack = mc.player.getInventory().getStack(armorSlots[i]);
            int y = startY + i * slotH;
            renderSlot(ctx, stack, x, y);
        }

        ItemStack offhand = mc.player.getOffHandStack();
        int offY = startY + 4 * slotH + 2;
        renderSlot(ctx, offhand, x, offY);
    }

    private void renderSlot(DrawContext ctx, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) return;

        ctx.drawItem(stack, x, y);
        ctx.drawItemInSlot(mc.textRenderer, stack, x, y);

        int textX = x + 18;

        if (showDurability.isEnabled() && stack.isDamageable()) {
            int maxDmg = stack.getMaxDamage();
            int dmg = stack.getDamage();
            float pct = maxDmg > 0 ? (float)(maxDmg - dmg) / maxDmg : 1f;
            int barColor = ColorUtil.healthColor(pct);

            int barWidth = (int)(13 * pct);
            ctx.fill(x + 1, y + 14, x + 14, y + 15, 0xFF000000);
            ctx.fill(x + 1, y + 14, x + 1 + barWidth, y + 15, barColor | 0xFF000000);

            int remaining = maxDmg - dmg;
            String label = remaining > 999 ? (remaining / 1000) + "k" : String.valueOf(remaining);
            ctx.drawTextWithShadow(mc.textRenderer, label, textX, y + 6, barColor | 0xFF000000);
            textX += mc.textRenderer.getWidth(label) + 3;
        }

        if (showEnchants.isEnabled()) {
            StringBuilder enchStr = new StringBuilder();
            //? if mc >= "1.20.5" {
            ItemEnchantmentsComponent enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
            if (enchantments != null && !enchantments.isEmpty()) {
                for (Map.Entry<RegistryEntry<Enchantment>, Integer> entry : enchantments.getEnchantmentEntries()) {
                    String abbr = abbreviate(entry.getKey().getIdAsString());
                    int lvl = entry.getValue();
                    if (!enchStr.isEmpty()) enchStr.append(" ");
                    enchStr.append(abbr);
                    if (lvl > 1) enchStr.append(lvl);
                }
            }
            //?} else {
            /*Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                String abbr = abbreviate(entry.getKey().getName(entry.getValue()).getString());
                int lvl = entry.getValue();
                if (!enchStr.isEmpty()) enchStr.append(" ");
                enchStr.append(abbr);
                if (lvl > 1) enchStr.append(lvl);
            }*/
            //?}
            if (!enchStr.isEmpty()) {
                ctx.drawTextWithShadow(mc.textRenderer, enchStr.toString(), x + 18, y, 0xFFAAAAAA);
            }
        }
    }

    private String abbreviate(String enchantmentId) {
        String name = enchantmentId.contains(":") ? enchantmentId.split(":")[1] : enchantmentId;
        String[] parts = name.split("_");
        if (parts.length == 1) return name.length() > 4 ? name.substring(0, 4) : name;
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) sb.append(Character.toUpperCase(part.charAt(0)));
        }
        return sb.toString();
    }
}
