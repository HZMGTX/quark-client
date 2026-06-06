package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

/**
 * FullInventoryWarning - Displays a HUD warning when the player's inventory is full.
 *
 * Checks all 36 main-inventory slots (hotbar + main grid, indices 0-35) and
 * shows a centered warning label when every slot is occupied. Optionally also
 * shows the current fill ratio.
 */
public class FullInventoryWarning extends Module {

    private final BoolSetting showRatio  = register(new BoolSetting("Show Ratio",   "Display used/total slot count",          true));
    private final BoolSetting flash      = register(new BoolSetting("Flash",        "Flash the warning text when full",       true));
    private final IntSetting  warnAt     = register(new IntSetting ("Warn At",      "Slots used to trigger warning (0=full)", 0, 0, 36));

    public FullInventoryWarning() {
        super("FullInventoryWarning", "Shows a HUD warning when the inventory is full or nearly full", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        // Count used slots in the main inventory (slots 0-35)
        int usedSlots = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty()) usedSlots++;
        }

        int threshold = warnAt.get() == 0 ? 36 : (36 - warnAt.get());
        boolean warn = usedSlots >= threshold;
        if (!warn) return;

        DrawContext ctx = event.getDrawContext();
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        // Flash logic: alternate visibility every 500 ms
        if (flash.isEnabled()) {
            long cycle = System.currentTimeMillis() / 500L;
            if ((cycle & 1L) == 0) return;
        }

        int lineH = mc.textRenderer.fontHeight + 2;
        int warningY = sh / 2 - 50;

        String warnText = usedSlots >= 36 ? "Inventory Full!" : "Inventory Almost Full!";
        int warnW = mc.textRenderer.getWidth(warnText);
        int warnX = sw / 2 - warnW / 2;

        int textColor = usedSlots >= 36 ? 0xFFFF4444 : 0xFFFFAA00;

        ctx.fill(warnX - 4, warningY - 2, warnX + warnW + 4, warningY + lineH + 2, 0xBB000000);
        ctx.drawTextWithShadow(mc.textRenderer, warnText, warnX, warningY, textColor);

        if (showRatio.isEnabled()) {
            String ratioText = usedSlots + " / 36 slots used";
            int ratioW = mc.textRenderer.getWidth(ratioText);
            int ratioX = sw / 2 - ratioW / 2;
            int ratioY = warningY + lineH + 2;
            ctx.fill(ratioX - 4, ratioY - 2, ratioX + ratioW + 4, ratioY + lineH + 2, 0xBB000000);
            ctx.drawTextWithShadow(mc.textRenderer, ratioText, ratioX, ratioY, 0xFFCCCCCC);
        }
    }
}
