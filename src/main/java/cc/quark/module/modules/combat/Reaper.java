package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class Reaper extends Module {

    private final IntSetting duration = register(new IntSetting("Duration", "Ticks to apply buffs after kill", 100, 20, 300));
    private final BoolSetting notify = register(new BoolSetting("Notify", "Show kill count in chat", true));

    private int killCount = 0;
    private int buffTicksLeft = 0;

    public Reaper() {
        super("Reaper", "Tracks kills and grants temporary speed and strength buffs", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        killCount = 0;
        buffTicksLeft = 0;
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.removeStatusEffect(StatusEffects.SPEED);
        mc.player.removeStatusEffect(StatusEffects.STRENGTH);
        buffTicksLeft = 0;
    }

    @Override
    public String getSuffix() {
        return "Kills: " + killCount;
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage().toLowerCase();
        if (mc.player == null) return;

        String playerName = mc.player.getGameProfile().getName().toLowerCase();

        boolean isKill = (msg.contains(playerName) && (msg.contains("killed") || msg.contains("slain") || msg.contains("was killed by")))
            || (msg.contains(playerName) && msg.contains("eliminated"));

        if (!isKill) return;

        killCount++;
        buffTicksLeft = duration.get();

        if (notify.isEnabled()) {
            ChatUtil.success("Kill #" + killCount + "! Buffs activated.");
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (buffTicksLeft > 0) {
            buffTicksLeft--;
            if (!mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, duration.get(), 1, false, false));
            }
            if (!mc.player.hasStatusEffect(StatusEffects.STRENGTH)) {
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, duration.get(), 0, false, false));
            }
            if (buffTicksLeft == 0) {
                mc.player.removeStatusEffect(StatusEffects.SPEED);
                mc.player.removeStatusEffect(StatusEffects.STRENGTH);
            }
        }
    }
}
