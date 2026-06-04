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

public class RageMode extends Module {

    private final DoubleSetting reachBonus = register(new DoubleSetting("Reach Bonus", "Extra attack reach in blocks", 0.5, 0.0, 2.0));
    private final BoolSetting forceSprint = register(new BoolSetting("Force Sprint", "Force sprint when enemy is in range", true));

    public RageMode() {
        super("RageMode", "Maximizes aggression: sprint attack, reach+, speed", Category.COMBAT);
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

        if (forceSprint.isEnabled()) {
            List<LivingEntity> targets = EntityUtil.getEntitiesOfType(LivingEntity.class, 6.0);
            targets.removeIf(e -> !(e instanceof PlayerEntity));
            targets.removeIf(EntityUtil::isFriend);

            if (!targets.isEmpty()) {
                mc.player.setSprinting(true);
            }
        }
    }

    public double getReachBonus() {
        return reachBonus.get();
    }
}
