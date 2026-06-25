package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DurabilityHUD extends Module {

    private final BoolSetting hotbar   = register(new BoolSetting("Hotbar", "Also show held hotbar item durability", true));
    private final IntSetting  posX     = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting  posY     = register(new IntSetting("Y", "HUD Y position", 4, 0, 3000));

    public DurabilityHUD() {
        super("DurabilityHUD", "Shows durability of all equipped items as progress bars", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 39; i >= 36; i--) stacks.add(mc.player.getInventory().getStack(i));
        stacks.add(mc.player.getOffHandStack());
        if (hotbar.isEnabled()) stacks.add(mc.player.getMainHandStack());

        int x = posX.get(), y = posY.get();
        int lh = mc.textRenderer.fontHeight + 4;

        for (ItemStack stack : stacks) {
            if (stack.isEmpty() || !stack.isDamageable()) continue;
            int maxDmg = stack.getMaxDamage();
            int remaining = maxDmg - stack.getDamage();
            float pct = maxDmg > 0 ? (float) remaining / maxDmg : 1f;
            int barColor = pct > 0.5f ? 0xFF55FF55 : pct > 0.25f ? 0xFFFFFF55 : 0xFFFF5555;

            String name = stack.getName().getString();
            String durStr = remaining + "/" + maxDmg;
            ctx.drawTextWithShadow(mc.textRenderer, name, x, y, 0xFFFFFFFF);
            ctx.drawTextWithShadow(mc.textRenderer, durStr, x + mc.textRenderer.getWidth(name) + 4, y, barColor);

            int barY = y + mc.textRenderer.fontHeight + 1;
            int barW = 80;
            int filled = (int) (barW * pct);
            ctx.fill(x, barY, x + barW, barY + 3, 0xFF333333);
            ctx.fill(x, barY, x + filled, barY + 3, barColor);
            y += lh + 4;
        }
    }
}
