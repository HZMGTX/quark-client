package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.InventoryUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoSoup2 extends Module {

    private final DoubleSetting healthThreshold = register(new DoubleSetting(
            "Health", "Eat when health drops below this value (hearts)", 14.0, 1.0, 19.0));

    private final TimerUtil timer = new TimerUtil();

    public AutoSoup2() {
        super("AutoSoup2", "Eats soup/gapple/golden carrot when HP is low, priority: Gapple > Golden Carrot > Soup", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.getHealth() > healthThreshold.get() * 2f) return;  // health is in half-hearts
        if (!timer.hasReached(200)) return;
        timer.reset();

        int slot = findBestFood();
        if (slot < 0 || slot >= 9) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prevSlot;
    }

    /** Returns hotbar slot of best food, with priority: Golden Apple > Golden Carrot > Stew/Soup */
    private int findBestFood() {
        // Priority 1: Golden Apple
        int slot = InventoryUtil.findItem(Items.GOLDEN_APPLE);
        if (slot >= 0 && slot < 9) return slot;

        // Priority 2: Golden Carrot
        slot = InventoryUtil.findItem(Items.GOLDEN_CARROT);
        if (slot >= 0 && slot < 9) return slot;

        // Priority 3: Mushroom Stew
        slot = InventoryUtil.findItem(Items.MUSHROOM_STEW);
        if (slot >= 0 && slot < 9) return slot;

        // Priority 4: Beetroot Soup
        slot = InventoryUtil.findItem(Items.BEETROOT_SOUP);
        if (slot >= 0 && slot < 9) return slot;

        // Priority 5: Rabbit Stew
        slot = InventoryUtil.findItem(Items.RABBIT_STEW);
        if (slot >= 0 && slot < 9) return slot;

        return -1;
    }
}
