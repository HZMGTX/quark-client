package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.LivingEntity;

import java.util.Comparator;
import java.util.Optional;

public class AntiHeal2 extends Module {
    private final BoolSetting interrupt = register(new BoolSetting("Interrupt", "Interrupt enemies eating/drinking", true));
    private final BoolSetting playersOnly = register(new BoolSetting("PlayersOnly", "Only target players", true));
    public AntiHeal2() { super("AntiHeal2", "Prevents enemies from healing by interrupting them", Category.COMBAT); }
    @EventHandler
    public void onTick(EventTick event) {
        if (!interrupt.getValue() || mc.player == null || mc.world == null) return;
        Optional<LivingEntity> healing = mc.world.getEntitiesByClass(LivingEntity.class,
            mc.player.getBoundingBox().expand(5), e -> e != mc.player && e.isUsingItem()).stream()
            .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(mc.player)));
        healing.ifPresent(e -> mc.interactionManager.attackEntity(mc.player, e));
    }
}
