package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;

public class AntiBlind extends Module {

    private final BoolSetting blindness  = register(new BoolSetting("Blindness",  "Remove blindness",      true));
    private final BoolSetting darkness   = register(new BoolSetting("Darkness",   "Remove darkness",       true));
    private final BoolSetting nausea     = register(new BoolSetting("Nausea",     "Remove nausea",         true));
    private final BoolSetting levitation = register(new BoolSetting("Levitation", "Remove levitation",     false));
    private final BoolSetting slowness   = register(new BoolSetting("Slowness",   "Remove slowness",       false));
    private final BoolSetting weakness   = register(new BoolSetting("Weakness",   "Remove weakness debuff",false));

    public AntiBlind() {
        super("AntiBlind", "Removes blindness, darkness and other debilitating visual effects", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (blindness.isEnabled())  mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
        if (darkness.isEnabled())   mc.player.removeStatusEffect(StatusEffects.DARKNESS);
        if (nausea.isEnabled())     mc.player.removeStatusEffect(StatusEffects.NAUSEA);
        if (levitation.isEnabled()) mc.player.removeStatusEffect(StatusEffects.LEVITATION);
        if (slowness.isEnabled())   mc.player.removeStatusEffect(StatusEffects.SLOWNESS);
        if (weakness.isEnabled())   mc.player.removeStatusEffect(StatusEffects.WEAKNESS);
    }
}
