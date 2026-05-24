package com.ghostclient.module.modules.render;

import com.ghostclient.GhostClient;
import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventRender3D;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.IntSetting;
import com.ghostclient.setting.ModeSetting;
import com.ghostclient.util.RenderUtil;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;

public class ItemESP extends Module {

    private final IntSetting range = register(new IntSetting("Range", "ESP range", 32, 10, 64));
    private final ModeSetting filter = register(new ModeSetting("Filter", "Item filter", "Valuable", "All", "Valuable", "Custom"));

    public ItemESP() {
        super("ItemESP", "Shows dropped items on the ground", Category.RENDER, 0);
    }

    @Override
    public void onEnable() {
        GhostClient.getInstance().getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        GhostClient.getInstance().getEventBus().unsubscribe(this);
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
            RenderUtil.drawESPBox(event.getMatrixStack(), ie.getBoundingBox(), color);
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
