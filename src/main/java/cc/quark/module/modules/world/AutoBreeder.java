package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AutoBreeder extends Module {

    private final DoubleSetting range   = register(new DoubleSetting(
            "Range",   "Breeding range",                3.0, 1.0, 8.0));
    private final IntSetting    delay   = register(new IntSetting(
            "Delay",   "Milliseconds between attempts", 800, 100, 5000));

    private final TimerUtil timer = new TimerUtil();

    public AutoBreeder() {
        super("AutoBreeder", "Automatically feeds and breeds nearby animals using food from your hotbar", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Find a food item in the hotbar that can breed at least one nearby animal
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (stack.isEmpty()) continue;

            for (Entity entity : mc.world.getEntities()) {
                if (!(entity instanceof AnimalEntity animal)) continue;
                if (mc.player.distanceTo(animal) > range.get()) continue;
                if (!animal.isBreedingItem(stack)) continue;
                if (animal.getLoveTicks() > 0) continue;
                if (animal.isBaby()) continue;

                // Switch hotbar to the food slot and interact
                int prevSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = slot;
                mc.interactionManager.interactEntity(mc.player, animal, Hand.MAIN_HAND);
                mc.player.getInventory().selectedSlot = prevSlot;

                timer.reset();
                return;
            }
        }
    }
}
