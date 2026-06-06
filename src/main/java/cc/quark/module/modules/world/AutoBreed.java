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
 * AutoBreed - automatically breeds nearby animals by right-clicking them
 * with the appropriate food item from the hotbar.
 *
 * Unlike AutoBreeder (which focuses on feeding), this module specifically
 * targets pairs of animals that are both ready to breed (not in love mode,
 * not babies) and uses the correct breeding food for each species.
 */
public class AutoBreed extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to find breedable animals", 4.0, 1.0, 10.0));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between breeding attempts", 600, 100, 5000));
    private final BoolSetting requirePair = register(new BoolSetting(
            "Require Pair", "Only breed when at least two breedable animals are nearby", true));
    private final BoolSetting skipBabies = register(new BoolSetting(
            "Skip Babies", "Do not feed babies even if the item is a breeding item", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoBreed() {
        super("AutoBreed", "Automatically breeds nearby animals with food from the hotbar", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (stack.isEmpty()) continue;

            // Count how many breedable animals accept this food
            int breedableCount = 0;
            AnimalEntity firstTarget = null;

            for (Entity entity : mc.world.getEntities()) {
                if (!(entity instanceof AnimalEntity animal)) continue;
                if (mc.player.distanceTo(animal) > range.get()) continue;
                if (skipBabies.isEnabled() && animal.isBaby()) continue;
                if (animal.getLoveTicks() > 0) continue;
                if (!animal.isBreedingItem(stack)) continue;
                breedableCount++;
                if (firstTarget == null) firstTarget = animal;
            }

            if (firstTarget == null) continue;
            if (requirePair.isEnabled() && breedableCount < 2) continue;

            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
            mc.interactionManager.interactEntity(mc.player, firstTarget, Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = prev;
            timer.reset();
            return;
        }
    }
}
