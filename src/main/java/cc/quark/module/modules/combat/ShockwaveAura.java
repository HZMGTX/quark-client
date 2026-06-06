package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Comparator;
import java.util.Optional;

public class ShockwaveAura extends Module {
    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 4.0, 1.0, 10.0));
    private final BoolSetting playersOnly = register(new BoolSetting("PlayersOnly", "Target players only", true));
    private int tick = 0;

    public ShockwaveAura() { super("ShockwaveAura", "Sends burst of attacks to knock enemies back", Category.COMBAT); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || ++tick < 3) return;
        tick = 0;
        Optional<LivingEntity> target = mc.world.getEntitiesByClass(LivingEntity.class,
            mc.player.getBoundingBox().expand(range.getValue()),
            e -> e != mc.player && (!playersOnly.getValue() || e instanceof PlayerEntity))
            .stream().min(Comparator.comparingDouble(e -> e.squaredDistanceTo(mc.player)));
        target.ifPresent(e -> mc.interactionManager.attackEntity(mc.player, e));
    }
}
