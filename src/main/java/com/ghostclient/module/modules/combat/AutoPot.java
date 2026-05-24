package com.ghostclient.module.modules.combat;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.DoubleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;

/**
 * AutoPot - automatically throws splash/lingering potions when the player's health
 * is below a configurable threshold or when specific buffs are desired.
 *
 * <p>Supports:
 * <ul>
 *   <li>Splash Potion of Healing (instant health)</li>
 *   <li>Splash Potion of Speed (movement buff)</li>
 *   <li>Splash Potion of Fire Resistance</li>
 * </ul>
 *
 * <p>After throwing, the module restores the previously held hotbar slot so the
 * player is not left holding a potion.
 */
public class AutoPot extends Module {

    private final DoubleSetting healthThreshold = register(new DoubleSetting(
            "Health Threshold", "Throw a healing potion when health is at or below this value",
            10.0, 1.0, 20.0));

    private final BoolSetting useHeal = register(new BoolSetting(
            "Use Heal", "Throw splash Potion of Healing when health is low", true));

    private final BoolSetting useSpeed = register(new BoolSetting(
            "Use Speed", "Throw splash Potion of Speed when not already speedy", false));

    private final BoolSetting useFire = register(new BoolSetting(
            "Use Fire Resistance", "Throw splash Potion of Fire Resistance when on fire", true));

    /** Cooldown in ticks between throws to avoid spamming. */
    private static final int THROW_COOLDOWN = 10;

    private int cooldown = 0;

    /** Hotbar slot held before auto-switching; -1 means no switch has occurred. */
    private int previousSlot = -1;

    public AutoPot() {
        super("AutoPot", "Automatically throws splash potions when health is low", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        cooldown = 0;
        previousSlot = -1;
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && previousSlot != -1) {
            mc.player.getInventory().selectedSlot = previousSlot;
        }
        previousSlot = -1;
    }

    @EventHandler
    public void onTick(EventTick event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        float health = mc.player.getHealth();

        // Determine which potion type we need right now, in priority order
        int targetSlot = -1;
        PotionType needed = null;

        if (useFire.isEnabled() && mc.player.isOnFire()) {
            int slot = findPotionSlot(mc, PotionType.FIRE_RESISTANCE);
            if (slot != -1) {
                targetSlot = slot;
                needed = PotionType.FIRE_RESISTANCE;
            }
        }

        if (targetSlot == -1 && useHeal.isEnabled() && health <= (float) healthThreshold.get()) {
            int slot = findPotionSlot(mc, PotionType.HEALING);
            if (slot != -1) {
                targetSlot = slot;
                needed = PotionType.HEALING;
            }
        }

        if (targetSlot == -1 && useSpeed.isEnabled()
                && !mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            int slot = findPotionSlot(mc, PotionType.SPEED);
            if (slot != -1) {
                targetSlot = slot;
                needed = PotionType.SPEED;
            }
        }

        if (targetSlot == -1) {
            // Nothing to throw; restore slot if we had switched
            if (previousSlot != -1) {
                mc.player.getInventory().selectedSlot = previousSlot;
                previousSlot = -1;
            }
            return;
        }

        // Switch to the potion slot
        if (mc.player.getInventory().selectedSlot != targetSlot) {
            if (previousSlot == -1) {
                previousSlot = mc.player.getInventory().selectedSlot;
            }
            mc.player.getInventory().selectedSlot = targetSlot;
        }

        // Look slightly downward so the potion hits the ground in front of us
        float savedPitch = mc.player.getPitch();
        mc.player.setPitch(85.0f);

        // Right-click to throw
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingMainHand();

        // Restore pitch
        mc.player.setPitch(savedPitch);

        // Restore previous slot after throw
        if (previousSlot != -1) {
            mc.player.getInventory().selectedSlot = previousSlot;
            previousSlot = -1;
        }

        cooldown = THROW_COOLDOWN;
    }

    // -------------------------------------------------------------------------
    // Potion type enum and detection
    // -------------------------------------------------------------------------

    private enum PotionType {
        HEALING, SPEED, FIRE_RESISTANCE
    }

    /**
     * Searches the hotbar (slots 0-8) for a splash or lingering potion of the given type.
     * Returns the slot index, or -1 if not found.
     */
    private int findPotionSlot(MinecraftClient mc, PotionType type) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            // Must be a splash or lingering potion
            if (stack.getItem() != Items.SPLASH_POTION && stack.getItem() != Items.LINGERING_POTION) {
                continue;
            }

            if (isPotionOfType(stack, type)) return i;
        }
        return -1;
    }

    /**
     * Returns true if the given item stack is a potion that provides the requested effect.
     */
    private boolean isPotionOfType(ItemStack stack, PotionType type) {
        PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (contents == null) return false;

        StatusEffect targetEffect = switch (type) {
            case HEALING        -> StatusEffects.INSTANT_HEALTH.value();
            case SPEED          -> StatusEffects.SPEED.value();
            case FIRE_RESISTANCE -> StatusEffects.FIRE_RESISTANCE.value();
        };

        // Check custom effects first
        for (var customEffect : contents.customEffects()) {
            if (customEffect.getEffectType().value() == targetEffect) return true;
        }

        // Check potion registry effects
        if (contents.potion().isPresent()) {
            var potion = contents.potion().get().value();
            for (var effect : potion.getEffects()) {
                if (effect.getEffectType().value() == targetEffect) return true;
            }
        }

        return false;
    }
}
