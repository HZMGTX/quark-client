package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.EntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.Comparator;
import java.util.List;

public class ArmorBreaker extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Attack range in blocks", 4, 1, 6));

    public ArmorBreaker() {
        super("ArmorBreaker", "Prioritizes attacking entities with low durability armor", Category.COMBAT);
    }

    private int getArmorDurabilityScore(LivingEntity entity) {
        int score = 0;
        for (ItemStack stack : entity.getArmorItems()) {
            if (stack.isEmpty()) continue;
            int maxDamage = stack.getMaxDamage();
            if (maxDamage <= 0) continue;
            int remaining = maxDamage - stack.getDamage();
            score += remaining;
        }
        return score;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getAttackCooldownProgress(0f) < 1.0f) return;

        List<LivingEntity> targets = EntityUtil.getEntitiesOfType(LivingEntity.class, range.get());
        targets.removeIf(e -> e == mc.player || e.isDead() || EntityUtil.isFriend(e));
        if (targets.isEmpty()) return;

        // Sort by lowest total armor durability remaining (easiest to break)
        targets.sort(Comparator.comparingInt(this::getArmorDurabilityScore));

        LivingEntity target = targets.get(0);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
