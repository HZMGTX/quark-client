package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.AxeItem;
import net.minecraft.registry.RegistryKeys;

import java.util.Comparator;

public class AutoSwitch2 extends Module {

    private final BoolSetting spider = register(new BoolSetting(
            "Spider", "Switch to bane-of-arthropods weapon for spiders", true));

    private final BoolSetting undead = register(new BoolSetting(
            "Undead", "Switch to smite weapon for undead mobs", true));

    public AutoSwitch2() {
        super("AutoSwitch2", "Auto-switches to correct weapon for entity type", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        Entity target = mc.targetedEntity;
        if (!(target instanceof LivingEntity living)) return;

        boolean isSpider = living instanceof SpiderEntity;
        boolean isUndead = living instanceof ZombieEntity || living instanceof SkeletonEntity;

        int bestSlot = -1;
        float bestScore = -1f;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof SwordItem) && !(stack.getItem() instanceof AxeItem)) continue;

            float score = 0f;
            var reg = mc.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);

            if (isSpider && spider.isEnabled()) {
                // Prefer bane of arthropods
                for (var entry : reg.streamEntries().toList()) {
                    var key = entry.getKey();
                    if (key.isPresent() && key.get().getValue().getPath().contains("bane_of_arthropods")) {
                        score += 10f;
                    }
                }
            } else if (isUndead && undead.isEnabled()) {
                // Prefer smite
                for (var entry : reg.streamEntries().toList()) {
                    var key = entry.getKey();
                    if (key.isPresent() && key.get().getValue().getPath().contains("smite")) {
                        score += 10f;
                    }
                }
            }

            if (stack.getItem() instanceof SwordItem sword) {
                score += sword.getMaterial().getAttackDamage();
            }

            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        if (bestSlot != -1) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }
}
