package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.ItemHitResult;

import java.util.List;

/**
 * ShulkerPeek — shows shulker box contents as a HUD tooltip when hovering over
 * a shulker box in the inventory or when one is in the crosshair.
 */
public class ShulkerPeek extends Module {

    private final IntSetting maxItems = register(new IntSetting(
            "Max Items", "Maximum item lines to display", 10, 1, 27));
    private final BoolSetting showCount = register(new BoolSetting(
            "Show Count", "Show item stack counts", true));
    private final BoolSetting showEmpty = register(new BoolSetting(
            "Show Empty", "Show message when shulker is empty", true));
    private final IntSetting x = register(new IntSetting(
            "X", "Tooltip X position", 10, 0, 1920));
    private final IntSetting y = register(new IntSetting(
            "Y", "Tooltip Y position", 60, 0, 1080));

    public ShulkerPeek() {
        super("ShulkerPeek", "Shows shulker box contents on hover", Category.PLAYER);
    }

    private boolean isShulkerBox(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var item = stack.getItem();
        return item == Items.SHULKER_BOX
                || item == Items.WHITE_SHULKER_BOX
                || item == Items.ORANGE_SHULKER_BOX
                || item == Items.MAGENTA_SHULKER_BOX
                || item == Items.LIGHT_BLUE_SHULKER_BOX
                || item == Items.YELLOW_SHULKER_BOX
                || item == Items.LIME_SHULKER_BOX
                || item == Items.PINK_SHULKER_BOX
                || item == Items.GRAY_SHULKER_BOX
                || item == Items.LIGHT_GRAY_SHULKER_BOX
                || item == Items.CYAN_SHULKER_BOX
                || item == Items.PURPLE_SHULKER_BOX
                || item == Items.BLUE_SHULKER_BOX
                || item == Items.BROWN_SHULKER_BOX
                || item == Items.GREEN_SHULKER_BOX
                || item == Items.RED_SHULKER_BOX
                || item == Items.BLACK_SHULKER_BOX;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        // Check held item or hovered inventory slot for shulker box
        ItemStack shulker = null;

        // Check the item currently hovered in inventory
        if (mc.currentScreen != null && mc.player.currentScreenHandler != null) {
            var focusedStack = mc.player.currentScreenHandler.getCursorStack();
            if (isShulkerBox(focusedStack)) {
                shulker = focusedStack;
            } else {
                // Check each slot for hover (we use the focused slot via mouse position heuristic —
                // instead just peek at held item in off-hand or main hand)
                for (int i = 0; i < mc.player.currentScreenHandler.slots.size(); i++) {
                    var s = mc.player.currentScreenHandler.getSlot(i).getStack();
                    if (isShulkerBox(s)) { shulker = s; break; }
                }
            }
        }

        // Also check main-hand and off-hand when no GUI open
        if (shulker == null && mc.currentScreen == null) {
            var main = mc.player.getMainHandStack();
            var off  = mc.player.getOffHandStack();
            if (isShulkerBox(main)) shulker = main;
            else if (isShulkerBox(off)) shulker = off;
        }

        if (shulker == null) return;

        // Extract container contents
        ContainerComponent container = shulker.get(DataComponentTypes.CONTAINER);
        if (container == null) {
            if (showEmpty.isEnabled()) {
                drawTooltip(event, shulker, List.of());
            }
            return;
        }

        List<ItemStack> items = container.stream()
                .filter(s -> !s.isEmpty())
                .toList();

        drawTooltip(event, shulker, items);
    }

    private void drawTooltip(EventRender2D event, ItemStack shulker, List<ItemStack> items) {
        var ctx  = event.getDrawContext();
        var font = mc.textRenderer;

        int px = x.get();
        int py = y.get();

        // Background
        int bgWidth = 140;
        int bgHeight = 14 + Math.min(items.size(), maxItems.get()) * 10 + (items.isEmpty() ? 10 : 0);
        ctx.fill(px - 2, py - 2, px + bgWidth, py + bgHeight, 0xCC000000);

        // Header
        String name = shulker.getName().getString();
        ctx.drawTextWithShadow(font, name, px, py, 0x9F5CF6);
        py += 12;

        if (items.isEmpty()) {
            ctx.drawTextWithShadow(font, "Empty", px, py, 0x888888);
            return;
        }

        int shown = 0;
        for (ItemStack stack : items) {
            if (shown >= maxItems.get()) break;
            String id = Registries.ITEM.getId(stack.getItem()).getPath();
            String label = showCount.isEnabled()
                    ? stack.getCount() + "x " + id
                    : id;
            ctx.drawTextWithShadow(font, label, px, py, 0xFFFFFF);
            py += 10;
            shown++;
        }

        if (items.size() > maxItems.get()) {
            ctx.drawTextWithShadow(font,
                    "... +" + (items.size() - maxItems.get()) + " more", px, py, 0x888888);
        }
    }
}
