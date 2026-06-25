package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.EntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public class CombatHelper extends Module {

    private final BoolSetting autoSprint = register(new BoolSetting("Auto Sprint", "Force sprint when enemies nearby", true));
    private final BoolSetting antiKB = register(new BoolSetting("Anti KB", "Reduce knockback taken", false));
    private final DoubleSetting reach = register(new DoubleSetting("Reach", "Extended reach range", 3.5, 3.0, 6.0));

    public CombatHelper() {
        super("CombatHelper", "All-in-one combat assistant: sprint, reach, crit", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (autoSprint.isEnabled()) {
            List<LivingEntity> nearby = EntityUtil.getEntitiesOfType(LivingEntity.class, 6.0);
            nearby.removeIf(e -> !(e instanceof PlayerEntity));
            nearby.removeIf(EntityUtil::isFriend);
            if (!nearby.isEmpty() && !mc.player.isTouchingWater() && !mc.player.isInLava()) {
                mc.player.setSprinting(true);
            }
        }

        if (antiKB.isEnabled()) {
            // Negate velocity from knockback by dampening XZ velocity
            if (mc.player.hurtTime > 0) {
                mc.player.setVelocity(
                        mc.player.getVelocity().x * 0.1,
                        mc.player.getVelocity().y,
                        mc.player.getVelocity().z * 0.1
                );
            }
        }
    }

    public double getReach() {
        return reach.get();
    }
}
