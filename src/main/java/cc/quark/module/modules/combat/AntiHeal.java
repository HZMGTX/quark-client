package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class AntiHeal extends Module {
    private final DoubleSetting range = register(new DoubleSetting("Range","Attack range to interrupt healing",4.0,1.0,8.0));
    public AntiHeal() { super("AntiHeal","Interrupts nearby players who are healing",Category.COMBAT); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player==null||mc.world==null||mc.interactionManager==null) return;
        for (var e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity p) || p==mc.player) continue;
            if (mc.player.distanceTo(p) > range.get()) continue;
            if (p.getHealth() < p.getMaxHealth() && mc.player.getAttackCooldownProgress(0f)>=1f) {
                mc.interactionManager.attackEntity(mc.player, p);
                mc.player.swingHand(Hand.MAIN_HAND);
                break;
            }
        }
    }
}
