package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class AntiFireAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to ignite nearby enemies", 4.0, 1.0, 8.0));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between ignite attempts", 1000, 100, 5000));

    private long lastIgnite = 0;

    public AntiFireAura() {
        super("AntiFireAura", "Ignites nearby enemies with fire aspect", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (System.currentTimeMillis() - lastIgnite < delay.get()) return;

        double r = range.get();
        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player) continue;
            if (living.isRemoved()) continue;
            if (mc.player.distanceTo(living) > r) continue;
            if (!living.isOnFire()) {
                // Simulate fire by setting fire ticks
                living.setOnFireFor(5);
                mc.interactionManager.attackEntity(mc.player, living);
                mc.player.swingHand(Hand.MAIN_HAND);
                lastIgnite = System.currentTimeMillis();
                break;
            }
        }
    }
}
