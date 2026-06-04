package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.Hand;

public class AntiCrystal extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to detect and break end crystals", 5.0, 1.0, 10.0));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between crystal breaks", 50, 0, 500));

    private long lastBreak = 0;

    public AntiCrystal() {
        super("AntiCrystal", "Breaks nearby end crystals automatically", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (System.currentTimeMillis() - lastBreak < delay.get()) return;

        double r = range.get();
        mc.world.getEntities().forEach(entity -> {
            if (entity instanceof EndCrystalEntity crystal) {
                if (mc.player.distanceTo(crystal) <= r) {
                    mc.interactionManager.attackEntity(mc.player, crystal);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    lastBreak = System.currentTimeMillis();
                }
            }
        });
    }
}
