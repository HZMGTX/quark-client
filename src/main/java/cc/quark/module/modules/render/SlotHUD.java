package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

/**
 * SlotHUD — renders all 9 hotbar slots (and optionally armor/offhand) as a
 * custom HUD element with item icons, stack counts, and durability bars.
 */
public class SlotHUD extends Module {

    private final ModeSetting  corner    = register(new ModeSetting( "Corner",    "Screen corner",                   "Bottom Right", "Bottom Right", "Bottom Left", "Top Right", "Top Left"));
    private final BoolSetting  showArmor = register(new BoolSetting( "Armor",     "Show armor slots above hotbar",    false));
    private final BoolSetting  showOff   = register(new BoolSetting( "Offhand",   "Show offhand slot",                true));
    private final BoolSetting  durBars   = register(new BoolSetting( "Dur Bars",  "Show durability bars under items", true));
    private final BoolSetting  highlight = register(new BoolSetting( "Highlight", "Highlight selected hotbar slot",   true));
    private final ColorSetting hlColor   = register(new ColorSetting("HL Color",  "Selected slot highlight color",    0xAA00AAFF));
    private final IntSetting   xPad      = register(new IntSetting(  "X Pad",     "Horizontal padding from edge",     4, 0, 100));
    private final IntSetting   yPad      = register(new IntSetting(  "Y Pad",     "Vertical padding from edge",       4, 0, 100));

    private static final int SLOT_SIZE = 18;
    private static final int SLOT_PAD  = 2;

    public SlotHUD() {
        super("SlotHUD", "Shows all hotbar slots with items, counts, and durability in a custom HUD", Category.RENDER);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int padX = xPad.get();
        int padY = yPad.get();

        // Total hotbar width: 9 slots
        int hotbarW = 9 * (SLOT_SIZE + SLOT_PAD) - SLOT_PAD;
        int hotbarH = SLOT_SIZE;

        // Base position (hotbar row)
        int baseX, baseY;
        switch (corner.get()) {
            case "Bottom Left" -> { baseX = padX;             baseY = sh - hotbarH - padY; }
            case "Top Right"   -> { baseX = sw - hotbarW - padX; baseY = padY; }
            case "Top Left"    -> { baseX = padX;             baseY = padY; }
            default            -> { baseX = sw - hotbarW - padX; baseY = sh - hotbarH - padY; } // Bottom Right
        }

        int selected = mc.player.getInventory().selectedSlot;

        // Hotbar background
        ctx.fill(baseX - 2, baseY - 2, baseX + hotbarW + 2, baseY + hotbarH + 2, 0x88000000);

        // Render armor slots above the hotbar if enabled
        if (showArmor.isEnabled()) {
            int[] armorSlots = {39, 38, 37, 36}; // head, chest, legs, feet
            for (int i = 0; i < 4; i++) {
                ItemStack stack = mc.player.getInventory().getStack(armorSlots[i]);
                int ax = baseX + i * (SLOT_SIZE + SLOT_PAD);
                int ay = baseY - SLOT_SIZE - SLOT_PAD - 4;
                renderSlot(ctx, stack, ax, ay, false);
            }
        }

        // Render offhand slot to the left of hotbar
        if (showOff.isEnabled()) {
            ItemStack offhand = mc.player.getOffHandStack();
            int ox = baseX - SLOT_SIZE - SLOT_PAD - 4;
            renderSlot(ctx, offhand, ox, baseY, false);
        }

        // Render main hotbar (slots 0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            int sx = baseX + i * (SLOT_SIZE + SLOT_PAD);
            int sy = baseY;

            // Selected slot highlight
            if (highlight.isEnabled() && i == selected) {
                ctx.fill(sx - 1, sy - 1, sx + SLOT_SIZE + 1, sy + SLOT_SIZE + 1, hlColor.get());
            }

            renderSlot(ctx, stack, sx, sy, durBars.isEnabled());
        }
    }

    private void renderSlot(DrawContext ctx, ItemStack stack, int x, int y, boolean drawDurability) {
        if (stack.isEmpty()) return;

        ctx.drawItem(stack, x, y);
        ctx.drawItemInSlot(mc.textRenderer, stack, x, y);

        if (drawDurability && stack.isDamageable()) {
            int maxDmg = stack.getMaxDamage();
            int dmg = stack.getDamage();
            float pct = maxDmg > 0 ? (float) (maxDmg - dmg) / maxDmg : 1f;

            if (pct < 1.0f) {
                // Red to green gradient
                int r = (int) ((1f - pct) * 255);
                int g = (int) (pct * 255);
                int barColor = 0xFF000000 | (r << 16) | (g << 8);

                int barW = (int) (SLOT_SIZE * pct);
                int barY = y + SLOT_SIZE - 1;
                ctx.fill(x, barY, x + SLOT_SIZE, barY + 1, 0xFF000000);
                ctx.fill(x, barY, x + barW, barY + 1, barColor);
            }
        }
    }
}
