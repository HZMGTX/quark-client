package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashSet;
import java.util.Set;

public class Notifier extends Module {

    private final BoolSetting sounds     = register(new BoolSetting("Sounds",     "Play a sound on notification",          true));
    private final BoolSetting joinLeave  = register(new BoolSetting("Join/Leave", "Notify when nearby players join/leave", true));
    private final BoolSetting lowHealth  = register(new BoolSetting("Low Health", "Notify when health is below 6 HP",      true));

    private final TimerUtil timer           = new TimerUtil();
    private Set<Integer>    trackedEntities = new HashSet<>();
    private boolean         healthWarned    = false;

    public Notifier() {
        super("Notifier", "Sends notifications for important game events", Category.MISC);
    }

    @Override
    public void onEnable() {
        trackedEntities.clear();
        healthWarned = false;
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null) return;

        if (lowHealth.isEnabled()) {
            float hp = mc.player.getHealth();
            if (hp > 0 && hp <= 6 && !healthWarned) {
                ChatUtil.warn("Low health! (" + (int) hp + " HP)");
                if (sounds.isEnabled()) mc.player.playSound(net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 1.0f, 1.0f);
                healthWarned = true;
            } else if (hp > 6) {
                healthWarned = false;
            }
        }

        if (!joinLeave.isEnabled() || !timer.hasReached(1500)) return;
        timer.reset();

        Set<Integer> current = new HashSet<>();
        for (var ent : mc.world.getEntities()) {
            if (!(ent instanceof PlayerEntity pe) || pe == mc.player) continue;
            if (mc.player.distanceTo(pe) > 64) continue;
            current.add(pe.getId());
            if (joinLeave.isEnabled() && !trackedEntities.contains(pe.getId()))
                ChatUtil.info(pe.getName().getString() + " entered range!");
        }
        for (int id : trackedEntities)
            if (!current.contains(id)) ChatUtil.warn("A player left range.");
        trackedEntities = current;
    }
}
