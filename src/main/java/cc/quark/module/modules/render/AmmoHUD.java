package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.*;
import net.minecraft.util.Hand;

public class AmmoHUD extends Module {

    public AmmoHUD() {
        super("AmmoHUD", "Shows arrow/firework count when holding a bow, crossbow, or elytra", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        ItemStack main   = mc.player.getMainHandStack();
        ItemStack off    = mc.player.getOffHandStack();
        Item mainItem    = main.getItem();

        boolean isBow      = mainItem instanceof BowItem;
        boolean isCrossbow = mainItem instanceof CrossbowItem;
        boolean isElytra   = mc.player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST).getItem() instanceof ElytraItem;

        if (!isBow && !isCrossbow && !isElytra) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        String label;
        if (isElytra) {
            int fireworks = countItem(FireworkRocketItem.class);
            label = "Fireworks: " + fireworks;
        } else {
            int arrows = countItem(ArrowItem.class) + countItem(SpectralArrowItem.class) + countItem(TippedArrowItem.class);
            label = "Arrows: " + arrows;
        }

        int x = sw / 2 - mc.textRenderer.getWidth(label) / 2;
        int y = sh - 58;
        RenderUtil.drawCustomText(ctx, label, x, y, 0xFFFFEE44);
    }

    private int countItem(Class<? extends Item> cls) {
        if (mc.player == null) return 0;
        int count = 0;
        for (ItemStack s : mc.player.getInventory().main) {
            if (cls.isInstance(s.getItem())) count += s.getCount();
        }
        if (cls.isInstance(mc.player.getOffHandStack().getItem())) count += mc.player.getOffHandStack().getCount();
        return count;
    }
}
