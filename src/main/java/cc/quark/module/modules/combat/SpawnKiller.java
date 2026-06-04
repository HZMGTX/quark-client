package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class SpawnKiller extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to detect and kill spawned entities", 5.0, 1.0, 10.0));

    private final BoolSetting mobs = register(new BoolSetting(
            "Mobs", "Kill spawned mobs", true));

    public SpawnKiller() {
        super("SpawnKiller", "Kills entities that spawn nearby", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getAttackCooldownProgress(0f) < 1f) return;

        double r = range.get();
        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player || living.isDead()) continue;
            if (living instanceof PlayerEntity) continue;
            if (!mobs.isEnabled() && living instanceof MobEntity) continue;
            if (mc.player.distanceTo(living) <= r) {
                mc.interactionManager.attackEntity(mc.player, living);
                mc.player.swingHand(Hand.MAIN_HAND);
                break;
            }
        }
    }
}
