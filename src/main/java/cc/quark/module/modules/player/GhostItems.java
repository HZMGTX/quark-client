package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GhostItems extends Module {

    private final IntSetting duration = register(new IntSetting(
            "Duration", "How long ghost item shows in milliseconds", 500, 100, 3000));

    private final List<GhostEntry> ghosts = new ArrayList<>();
    private ItemStack lastHeld = ItemStack.EMPTY;

    public GhostItems() {
        super("GhostItems", "Shows ghost of swapped items briefly", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        ghosts.clear();
        lastHeld = ItemStack.EMPTY;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        ItemStack held = mc.player.getMainHandStack();

        if (!ItemStack.areItemsAndComponentsEqual(held, lastHeld)) {
            if (!lastHeld.isEmpty()) {
                ghosts.add(new GhostEntry(lastHeld.copy(), System.currentTimeMillis()));
            }
            lastHeld = held.copy();
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        long now = System.currentTimeMillis();
        int scaledWidth = mc.getWindow().getScaledWidth();
        int scaledHeight = mc.getWindow().getScaledHeight();

        Iterator<GhostEntry> iter = ghosts.iterator();
        int offsetX = 0;
        while (iter.hasNext()) {
            GhostEntry ghost = iter.next();
            long elapsed = now - ghost.time;
            if (elapsed > duration.get()) {
                iter.remove();
                continue;
            }

            float alpha = 1.0f - (float) elapsed / duration.get();
            int alphaInt = (int) (alpha * 200);

            int px = scaledWidth / 2 - 8 + offsetX;
            int py = scaledHeight - 40;

            ctx.drawItem(ghost.stack, px, py);
            offsetX += 20;
        }
    }

    private static class GhostEntry {
        final ItemStack stack;
        final long time;
        GhostEntry(ItemStack stack, long time) {
            this.stack = stack;
            this.time = time;
        }
    }
}
