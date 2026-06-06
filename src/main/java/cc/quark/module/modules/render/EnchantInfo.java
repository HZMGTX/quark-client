package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * EnchantInfo - HUD that lists the enchantments on the currently held item.
 *
 * Reads the enchantment component from the main-hand ItemStack and renders
 * each enchantment name + level on screen at a configurable position.
 */
public class EnchantInfo extends Module {

    private final IntSetting posX      = register(new IntSetting("X",         "HUD X position",               5,   0, 3840));
    private final IntSetting posY      = register(new IntSetting("Y",         "HUD Y position",               60,  0, 2160));
    private final BoolSetting showBook = register(new BoolSetting("Books",    "Include book enchantments",    true));
    private final BoolSetting showHead = register(new BoolSetting("Header",   "Show 'Enchantments:' header",  true));

    public EnchantInfo() {
        super("EnchantInfo", "Shows enchantments on the held item as a HUD overlay", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        ItemStack held = mc.player.getMainHandStack();
        if (held.isEmpty()) return;

        ItemEnchantmentsComponent encComp = held.getOrDefault(
                DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

        if (encComp.isEmpty()) return;

        DrawContext ctx = event.getDrawContext();
        int x    = posX.get();
        int y    = posY.get();
        int lineH = mc.textRenderer.fontHeight + 2;

        // Collect enchantment lines
        List<String> lines = new ArrayList<>();
        for (RegistryEntry<Enchantment> entry : encComp.getEnchantments()) {
            int level = encComp.getLevel(entry);
            Text name = entry.value().description();
            String levelStr = level > 1 ? " " + toRoman(level) : "";
            lines.add(name.getString() + levelStr);
        }

        if (lines.isEmpty()) return;

        // Measure background width
        int maxW = 0;
        if (showHead.isEnabled()) maxW = mc.textRenderer.getWidth("Enchantments:");
        for (String l : lines) {
            int w = mc.textRenderer.getWidth(l);
            if (w > maxW) maxW = w;
        }

        int totalLines = lines.size() + (showHead.isEnabled() ? 1 : 0);
        int bgH = totalLines * lineH + 2;

        ctx.fill(x - 2, y - 2, x + maxW + 4, y + bgH, 0xAA111111);

        if (showHead.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, "Enchantments:", x, y, 0xFFFFAA00);
            y += lineH;
        }

        for (String line : lines) {
            ctx.drawTextWithShadow(mc.textRenderer, line, x, y, 0xFFAAFFAA);
            y += lineH;
        }
    }

    private String toRoman(int n) {
        return switch (n) {
            case 1  -> "I";
            case 2  -> "II";
            case 3  -> "III";
            case 4  -> "IV";
            case 5  -> "V";
            case 6  -> "VI";
            case 7  -> "VII";
            case 8  -> "VIII";
            case 9  -> "IX";
            case 10 -> "X";
            default -> String.valueOf(n);
        };
    }
}
