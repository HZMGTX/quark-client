package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.EntityUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.RegistryKeys;

import java.util.Comparator;
import java.util.List;

/**
 * AutoSmite — switches to a Smite-enchanted weapon when the KillAura target is undead.
 */
public class AutoSmite extends Module {

    private final BoolSetting switchBack = register(new BoolSetting(
            "SwitchBack", "Switch back to previous slot after attacking", true));

    private int prevSlot = -1;

    public AutoSmite() {
        super("AutoSmite", "Switches to Smite weapon against undead mobs", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Find nearest undead entity
        LivingEntity undeadTarget = findNearestUndead();
        if (undeadTarget == null) {
            restoreSlot();
            return;
        }

        int smiteSlot = findSmiteSlot();
        if (smiteSlot == -1) {
            restoreSlot();
            return;
        }

        int currentSlot = mc.player.getInventory().selectedSlot;
        if (currentSlot != smiteSlot) {
            if (prevSlot == -1) prevSlot = currentSlot;
            mc.player.getInventory().selectedSlot = smiteSlot;
        }
    }

    private LivingEntity findNearestUndead() {
        List<LivingEntity> entities = EntityUtil.getEntitiesOfType(LivingEntity.class, 6.0);
        for (LivingEntity e : entities.stream()
                .sorted(Comparator.comparingDouble(EntityUtil::distanceTo))
                .toList()) {
            if (isUndead(e)) return e;
        }
        return null;
    }

    private boolean isUndead(Entity entity) {
        return entity instanceof ZombieEntity
                || entity instanceof SkeletonEntity
                || entity instanceof DrownedEntity
                || entity instanceof PhantomEntity
                || entity instanceof ZombieVillagerEntity;
    }

    private int findSmiteSlot() {
        int bestSlot = -1;
        int bestLevel = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof SwordItem)) continue;
            // Check for Smite enchantment
            var registries = mc.world.getRegistryManager();
            var enchantReg = registries.get(RegistryKeys.ENCHANTMENT);
            for (var entry : enchantReg.getIndexedEntries()) {
                // Look for smite by name check
                String id = enchantReg.getId(entry.value()) != null
                        ? enchantReg.getId(entry.value()).getPath() : "";
                if (id.equals("smite")) {
                    int level = EnchantmentHelper.getLevel(entry, stack);
                    if (level > bestLevel) {
                        bestLevel = level;
                        bestSlot = i;
                    }
                    break;
                }
            }
        }
        return bestSlot;
    }

    private void restoreSlot() {
        if (prevSlot != -1 && mc.player != null && switchBack.isEnabled()) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }
}
