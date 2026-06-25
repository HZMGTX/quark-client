package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.MaceItem;
import net.minecraft.util.Hand;

import java.util.Comparator;
import java.util.List;

public class MaceAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range in blocks", 4.0, 1.0, 6.0));
    private final BoolSetting onlyFalling = register(new BoolSetting("OnlyFalling", "Only swing while falling for wind burst bonus", true));

    public MaceAura() {
        super("MaceAura", "Auto-swings mace at nearby entities with wind burst damage", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof MaceItem)) return;
        if (onlyFalling.isEnabled() && mc.player.getVelocity().y >= 0) return;
        if (mc.player.getAttackCooldownProgress(0f) < 1.0f) return;

        List<LivingEntity> targets = EntityUtil.getEntitiesOfType(LivingEntity.class, range.get());
        targets.removeIf(e -> e == mc.player || e.isDead() || EntityUtil.isFriend(e));
        targets.sort(Comparator.comparingDouble(EntityUtil::distanceTo));

        if (!targets.isEmpty()) {
            LivingEntity target = targets.get(0);
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
