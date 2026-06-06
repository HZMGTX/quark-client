package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * AutoPot - automatically throws or uses potions from the hotbar when
 * relevant conditions are met (low health, in combat, etc.).
 *
 * Supports splash potions (thrown), instant potions (drunk), and
 * lingering potions. Configurable health threshold and item-type filters.
 */
public class AutoPot extends Module {

    private final DoubleSetting healthThreshold = register(new DoubleSetting(
            "Health Threshold", "Auto-use health potions below this HP", 14.0, 1.0, 20.0));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between potion uses", 1000, 200, 5000));
    private final BoolSetting useSplash = register(new BoolSetting(
            "Splash", "Use splash potions (thrown)", true));
    private final BoolSetting useDrink = register(new BoolSetting(
            "Drink", "Use drinkable potions (right-click hold)", true));
    private final BoolSetting useLingering = register(new BoolSetting(
            "Lingering", "Use lingering potions (thrown)", false));
    private final BoolSetting onlyInCombat = register(new BoolSetting(
            "Only In Combat", "Only use potions while receiving damage (recently hurt)", false));
    private final BoolSetting preferSplash = register(new BoolSetting(
            "Prefer Splash", "Prefer splash potions over drinkable when both are available", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoPot() {
        super("AutoPot", "Automatically uses health potions from the hotbar", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;
        if (mc.player.getHealth() >= healthThreshold.get()) return;
        if (onlyInCombat.isEnabled() && mc.player.hurtTime <= 0) return;

        // Search hotbar for a potion
        int splashSlot  = -1;
        int drinkSlot   = -1;
        int lingerSlot  = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();
            if (useSplash.isEnabled()   && item == Items.SPLASH_POTION   && splashSlot  == -1) splashSlot  = i;
            if (useDrink.isEnabled()    && item == Items.POTION           && drinkSlot   == -1) drinkSlot   = i;
            if (useLingering.isEnabled()&& item == Items.LINGERING_POTION && lingerSlot  == -1) lingerSlot  = i;
        }

        int chosen = -1;
        boolean thrown = false;

        if (preferSplash.isEnabled()) {
            if (splashSlot != -1) { chosen = splashSlot; thrown = true; }
            else if (lingerSlot != -1) { chosen = lingerSlot; thrown = true; }
            else if (drinkSlot != -1) { chosen = drinkSlot; }
        } else {
            if (drinkSlot != -1) { chosen = drinkSlot; }
            else if (splashSlot != -1) { chosen = splashSlot; thrown = true; }
            else if (lingerSlot != -1) { chosen = lingerSlot; thrown = true; }
        }

        if (chosen == -1) return;

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = chosen;

        if (thrown) {
            // Look slightly downward so it lands at feet, then throw
            float savedPitch = mc.player.getPitch();
            mc.player.setPitch(85.0f);
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.setPitch(savedPitch);
        } else {
            // Drinkable potion: start using
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }

        mc.player.getInventory().selectedSlot = prev;
        timer.reset();
    }
}
