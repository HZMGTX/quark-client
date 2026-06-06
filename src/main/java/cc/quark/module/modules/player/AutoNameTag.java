package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * AutoNameTag - Automatically renames the entity you are looking at by using
 * a name tag from your inventory/hotbar.
 */
public class AutoNameTag extends Module {

    private final StringSetting customName = register(new StringSetting(
            "Name", "Name to apply to the looked-at entity", "Quark"));

    private final BoolSetting requireSneak = register(new BoolSetting(
            "Require Sneak", "Only apply name tag while sneaking", true));

    private final BoolSetting onlyMobs = register(new BoolSetting(
            "Only Mobs", "Only apply to living entities (no minecarts, boats...)", true));

    private final IntSetting cooldownTicks = register(new IntSetting(
            "Cooldown", "Ticks between successive name-tag uses", 20, 5, 100));

    private int tickCooldown = 0;

    public AutoNameTag() {
        super("AutoNameTag", "Renames the entity you're looking at using name tags in your inventory", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (tickCooldown > 0) {
            tickCooldown--;
            return;
        }

        if (requireSneak.isEnabled() && !mc.player.isSneaking()) return;

        // Find a name tag in the hotbar or inventory
        int nameTagSlot = findNameTagSlot();
        if (nameTagSlot == -1) return;

        // Check what the player is looking at
        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) return;

        EntityHitResult entityHit = (EntityHitResult) hit;
        Entity target = entityHit.getEntity();

        if (onlyMobs.isEnabled() && !(target instanceof LivingEntity)) return;

        // Switch to the name-tag slot, use it on the entity, switch back
        int prevSlot = mc.player.getInventory().selectedSlot;

        if (nameTagSlot < 9) {
            mc.player.getInventory().selectedSlot = nameTagSlot;
        }

        mc.interactionManager.interactEntity(mc.player, target, Hand.MAIN_HAND);

        // Restore original slot if we changed it
        if (nameTagSlot < 9 && nameTagSlot != prevSlot) {
            mc.player.getInventory().selectedSlot = prevSlot;
        }

        tickCooldown = cooldownTicks.get();
        ChatUtil.info("[AutoNameTag] Applied name tag to " + target.getType().getName().getString());
    }

    /** Returns the first hotbar slot (0-8) that contains a name tag, or -1 if none found. */
    private int findNameTagSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == Items.NAME_TAG) {
                return i;
            }
        }
        // Also check main inventory (slots 9-35) - can't use directly but signal presence
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == Items.NAME_TAG) {
                // For simplicity report unavailable (requires inventory management)
                ChatUtil.warn("[AutoNameTag] Name tag found in inventory but not hotbar.");
                return -1;
            }
        }
        return -1;
    }
}
