package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * AutoFeed - Automatically feeds nearby animals with food from inventory.
 * Unlike AutoBreeder, this feeds animals that are hungry regardless of love state,
 * and searches the full inventory (not just hotbar).
 */
public class AutoFeed extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to scan for animals", 5.0, 1.0, 10.0));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between feed attempts", 600, 100, 5000));
    private final BoolSetting babies = register(new BoolSetting(
            "Babies", "Also feed baby animals", false));
    private final BoolSetting fullInv = register(new BoolSetting(
            "FullInventory", "Search full inventory, not just hotbar", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoFeed() {
        super("AutoFeed", "Automatically feeds nearby animals with food from inventory", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        int maxSlot = fullInv.isEnabled() ? 36 : 9;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof AnimalEntity animal)) continue;
            if (mc.player.distanceTo(animal) > range.get()) continue;
            if (!babies.isEnabled() && animal.isBaby()) continue;
            if (animal.getLoveTicks() > 0) continue;

            // Find a food slot in inventory that the animal accepts
            int feedSlot = findFoodSlot(animal, maxSlot);
            if (feedSlot < 0) continue;

            int prevSlot = mc.player.getInventory().selectedSlot;

            // Map full-inventory slot to hotbar if needed
            if (feedSlot < 9) {
                mc.player.getInventory().selectedSlot = feedSlot;
            } else {
                // Swap the item into hotbar slot 0 temporarily
                mc.player.getInventory().selectedSlot = 0;
                mc.player.getInventory().swapSlotWithHotbar(feedSlot);
            }

            mc.interactionManager.interactEntity(mc.player, animal, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);

            // Restore
            if (feedSlot >= 9) {
                mc.player.getInventory().swapSlotWithHotbar(feedSlot);
            }
            mc.player.getInventory().selectedSlot = prevSlot;

            timer.reset();
            return;
        }
    }

    private int findFoodSlot(AnimalEntity animal, int maxSlot) {
        if (mc.player == null) return -1;
        var inv = mc.player.getInventory();
        for (int i = 0; i < maxSlot; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && animal.isBreedingItem(stack)) return i;
        }
        return -1;
    }
}
