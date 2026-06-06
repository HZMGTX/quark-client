package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

public class ArmorBreakAlert extends Module {
    private final IntSetting threshold = register(new IntSetting("Threshold", "Durability % to alert", 15, 1, 50));
    private boolean[] alerted = new boolean[4];

    public ArmorBreakAlert() { super("ArmorBreakAlert", "Warns when armor is about to break", Category.RENDER); }
    @Override public void onEnable() { alerted = new boolean[4]; }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        var armor = mc.player.getInventory().armor;
        for (int i = 0; i < 4; i++) {
            ItemStack stack = armor.get(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) { alerted[i] = false; continue; }
            int maxDmg = stack.getMaxDamage();
            int dmg = stack.getDamage();
            int pct = maxDmg > 0 ? (int)((1f - (float)dmg / maxDmg) * 100) : 100;
            if (pct <= threshold.get() && !alerted[i]) {
                ChatUtil.warn("Armor piece " + i + " durability critical: " + pct + "%!");
                alerted[i] = true;
            } else if (pct > threshold.get()) alerted[i] = false;
        }
    }
}
