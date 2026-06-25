package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public class InfinityKill extends Module {
    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 4.0, 1.0, 10.0));
    private final BoolSetting ignoreTeam = register(new BoolSetting("IgnoreTeam", "Attack teammates too", false));
    private int tick = 0;

    public InfinityKill() { super("InfinityKill", "Attacks all nearby enemies every tick", Category.COMBAT); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || ++tick < 2) return;
        tick = 0;
        List<LivingEntity> targets = mc.world.getEntitiesByClass(LivingEntity.class,
            mc.player.getBoundingBox().expand(range.getValue()), e -> e != mc.player);
        targets.forEach(e -> mc.interactionManager.attackEntity(mc.player, e));
    }
}
