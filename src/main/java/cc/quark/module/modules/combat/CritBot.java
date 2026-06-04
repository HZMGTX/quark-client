package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.EntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public class CritBot extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Range to trigger crit jump", 3.0, 1.0, 6.0));

    public CritBot() {
        super("CritBot", "Automatically jumps to land critical hits", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.isTouchingWater() || mc.player.isInLava()) return;

        List<LivingEntity> targets = EntityUtil.getEntitiesOfType(LivingEntity.class, range.get());
        targets.removeIf(e -> e == mc.player || e.isDead());
        targets.removeIf(e -> !(e instanceof PlayerEntity));

        if (targets.isEmpty()) return;

        // Jump to get critical hit multiplier
        mc.player.jump();
    }
}
