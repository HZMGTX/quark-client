package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.ExperienceOrbEntity;

public class XPFarm extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to auto-collect XP orbs (blocks)", 4.0, 1.0, 16.0));

    public XPFarm() {
        super("XPFarm", "Auto-collects XP orbs nearby", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        double r = range.get();
        // Move towards the nearest XP orb within range
        mc.world.getEntitiesByClass(ExperienceOrbEntity.class,
                mc.player.getBoundingBox().expand(r),
                orb -> orb.squaredDistanceTo(mc.player) <= r * r
        ).stream()
                .min((a, b) -> Double.compare(
                        a.squaredDistanceTo(mc.player),
                        b.squaredDistanceTo(mc.player)))
                .ifPresent(orb -> {
                    // Nudge player velocity toward the orb (client-side attraction assist)
                    double dx = orb.getX() - mc.player.getX();
                    double dz = orb.getZ() - mc.player.getZ();
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist > 0.1) {
                        double speed = 0.15;
                        mc.player.setVelocity(
                                mc.player.getVelocity().add(dx / dist * speed, 0, dz / dist * speed));
                    }
                });
    }
}
