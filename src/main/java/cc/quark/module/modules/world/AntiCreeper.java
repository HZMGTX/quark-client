package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.Hand;

public class AntiCreeper extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Detection range for creepers", 4.0, 1.0, 10.0));
    private final BoolSetting   kill  = register(new BoolSetting  ("Kill",  "Kill creepers on detection",   true));
    private final BoolSetting   push  = register(new BoolSetting  ("Push",  "Push creepers away instead",   false));

    public AntiCreeper() {
        super("AntiCreeper", "Detects and neutralizes nearby creepers before explosion", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof CreeperEntity creeper)) continue;
            if (mc.player.distanceTo(creeper) > range.get()) continue;

            if (push.isEnabled()) {
                double dx = creeper.getX() - mc.player.getX();
                double dz = creeper.getZ() - mc.player.getZ();
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len > 0) creeper.addVelocity(dx / len * 0.5, 0.2, dz / len * 0.5);
            }

            if (kill.isEnabled()) {
                mc.interactionManager.attackEntity(mc.player, creeper);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }
}
