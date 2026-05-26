package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.entity.effect.StatusEffects;

public class AntiPoison extends Module {

    public AntiPoison() {
        super("AntiPoisonPlayer", "Removes poison and wither status effects (player category)", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        mc.player.removeStatusEffect(StatusEffects.POISON);
        mc.player.removeStatusEffect(StatusEffects.WITHER);
    }
}
