package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
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

public class EnchantHUD extends Module {

    private final BoolSetting showLevels = register(new BoolSetting("ShowLevels", "Show enchantment level numbers", true));
    private final IntSetting  posX       = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting  posY       = register(new IntSetting("Y", "HUD Y position", 4, 0, 3000));

    public EnchantHUD() {
        super("EnchantHUD", "Lists enchantments of the held item in a compact HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        ItemStack stack = mc.player.getMainHandStack();
        if (stack.isEmpty()) return;

        DrawContext ctx = event.getDrawContext();
        int x = posX.get(), y = posY.get();
        int lh = mc.textRenderer.fontHeight + 2;

        ctx.drawTextWithShadow(mc.textRenderer, stack.getName().getString(), x, y, 0xFFFFFFFF);
        y += lh;

        //? if mc >= "1.20.5" {
        ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchants == null || enchants.isEmpty()) return;
        for (Map.Entry<RegistryEntry<Enchantment>, Integer> e : enchants.getEnchantmentEntries()) {
            String id = e.getKey().getIdAsString();
            String name = id.contains(":") ? id.split(":")[1] : id;
            name = capitalize(name.replace("_", " "));
            int lvl = e.getValue();
            String label = showLevels.isEnabled() ? name + " " + lvl : name;
            ctx.drawTextWithShadow(mc.textRenderer, label, x, y, 0xFFFFAA00);
            y += lh;
        }
        //?} else {
        /*Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
        if (enchants.isEmpty()) return;
        for (Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
            String name = capitalize(e.getKey().getName(e.getValue()).getString());
            String label = showLevels.isEnabled() ? name + " " + e.getValue() : name;
            ctx.drawTextWithShadow(mc.textRenderer, label, x, y, 0xFFFFAA00);
            y += lh;
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
