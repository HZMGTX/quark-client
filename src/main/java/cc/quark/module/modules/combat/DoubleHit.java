package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.util.Hand;

/**
 * DoubleHit - sends the attack interaction twice to deal extra damage.
 * A configurable chance prevents 100% consistency that could flag anti-cheats.
 */
public class DoubleHit extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Millisecond delay between the two hits", 2, 0, 10));

    private final DoubleSetting chance = register(new DoubleSetting(
            "Chance", "Probability (%) the second hit fires", 80.0, 0.0, 100.0));

    public DoubleHit() {
        super("DoubleHit", "Sends attack packet twice for extra damage", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (Math.random() * 100.0 > chance.get()) return;

        int ms = delay.get();
        if (ms > 0) {
            Thread t = new Thread(() -> {
                try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
                if (mc.player != null && mc.interactionManager != null) {
                    mc.interactionManager.attackEntity(mc.player, event.getTarget());
                    mc.player.swingHand(Hand.MAIN_HAND);
                }
            });
            t.setDaemon(true);
            t.start();
        } else {
            mc.interactionManager.attackEntity(mc.player, event.getTarget());
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
