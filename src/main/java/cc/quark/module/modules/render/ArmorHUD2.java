package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

public class ArmorHUD2 extends Module {
    private final IntSetting x = register(new IntSetting("X", "X position", 2, 0, 1000));
    private final IntSetting y = register(new IntSetting("Y", "Y position", 50, 0, 600));

    public ArmorHUD2() { super("ArmorHUD2", "Compact armor durability HUD with bars", Category.RENDER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (mc.player == null) return;
        DrawContext ctx = e.getDrawContext();
        var armorSlots = mc.player.getInventory().armor;
        int px = x.get(), py = y.get();
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = armorSlots.get(i);
            if (stack.isEmpty()) continue;
            int maxDmg = stack.getMaxDamage();
            if (maxDmg == 0) continue;
            float pct = 1f - (float) stack.getDamage() / maxDmg;
            int barColor = pct > 0.5f ? 0x55FF55 : (pct > 0.25f ? 0xFFFF55 : 0xFF5555);
            ctx.fill(px, py, px + 40, py + 4, ColorUtil.withAlpha(0x333333, 180));
            ctx.fill(px, py, px + (int)(40 * pct), py + 4, ColorUtil.withAlpha(barColor, 220));
            cc.quark.util.RenderUtil.drawCustomText(ctx, stack.getName().getString().substring(0, Math.min(8, stack.getName().getString().length())), px + 43, py - 1, 0xFFCCCCCC);
            py += 8;
        }
    }
}
