package cc.quark.module.modules.render;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;

public class ItemESP extends Module {

    private final IntSetting range = register(new IntSetting("Range", "ESP range", 32, 10, 64));
    private final ModeSetting filter = register(new ModeSetting("Filter", "Item filter", "Valuable", "All", "Valuable", "Custom"));

    public ItemESP() {
        super("ItemESP", "Shows dropped items on the ground", Category.RENDER);
    }

    

    

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity ie)) continue;
            if (mc.player.distanceTo(ie) > range.getValue()) continue;
            Item item = ie.getStack().getItem();
            if (!filter.getValue().equals("All") && !isValuable(item)) continue;
            int color = getColor(item);
            float cr = ((color >> 16) & 0xFF) / 255.0f;
            float cg = ((color >> 8) & 0xFF) / 255.0f;
            float cb = (color & 0xFF) / 255.0f;
            RenderUtil.drawESPBox(event.getMatrixStack(), ie.getBoundingBox(), cr, cg, cb, 0.85f, 1.5f);
        }
    }

    private boolean isValuable(Item item) {
        return item == Items.DIAMOND || item == Items.NETHERITE_INGOT ||
               item == Items.EMERALD || item == Items.GOLD_INGOT ||
               item == Items.TOTEM_OF_UNDYING || item == Items.ENCHANTED_GOLDEN_APPLE ||
               item instanceof SwordItem || item instanceof ArmorItem;
    }

    private int getColor(Item item) {
        if (item == Items.DIAMOND || item == Items.DIAMOND_SWORD) return 0xFF55FFFF;
        if (item == Items.NETHERITE_INGOT) return 0xFF443355;
        if (item == Items.TOTEM_OF_UNDYING) return 0xFFFFAA00;
        if (item == Items.ENCHANTED_GOLDEN_APPLE) return 0xFFFFD700;
        return 0xFFFFFFFF;
    }
}
