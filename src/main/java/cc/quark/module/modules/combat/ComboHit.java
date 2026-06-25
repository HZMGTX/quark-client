package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Comparator;
import java.util.Optional;

public class ComboHit extends Module {
    private final IntSetting comboLength = register(new IntSetting("ComboLength", "Number of hits in combo", 3, 2, 10));
    private final BoolSetting playersOnly = register(new BoolSetting("PlayersOnly", "Only target players", true));
    private int hitCount = 0;
    private int tickDelay = 0;

    public ComboHit() { super("ComboHit", "Performs automatic hit combos on targets", Category.COMBAT); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || ++tickDelay < 4) return;
        tickDelay = 0;
        if (hitCount >= comboLength.getValue()) { hitCount = 0; return; }
        Optional<LivingEntity> target = mc.world.getEntitiesByClass(LivingEntity.class,
            mc.player.getBoundingBox().expand(3.5), e -> e != mc.player &&
            (!playersOnly.getValue() || e instanceof PlayerEntity)).stream()
            .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(mc.player)));
        target.ifPresent(e -> { mc.interactionManager.attackEntity(mc.player, e); hitCount++; });
    }
}
