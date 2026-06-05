package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.player.PlayerEntity;

public class Notifier extends Module {
    private final BoolSetting playerJoin = register(new BoolSetting("Player Join", "Notify when player enters range", true));
    private final BoolSetting playerLeave = register(new BoolSetting("Player Leave", "Notify when player leaves range", false));
    private final IntSetting range = register(new IntSetting("Range", "Detection range", 20, 5, 128));
    private final TimerUtil timer = new TimerUtil();
    private java.util.Set<Integer> trackedEntities = new java.util.HashSet<>();

    public Notifier() { super("Notifier", "Notifies on player join/leave in range", Category.MISC); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); trackedEntities.clear(); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null || !timer.hasReached(1000)) return;
        java.util.Set<Integer> current = new java.util.HashSet<>();
        for (var ent : mc.world.getEntities()) {
            if (!(ent instanceof PlayerEntity pe) || pe == mc.player) continue;
            if (mc.player.distanceTo(pe) > range.get()) continue;
            current.add(pe.getId());
            if (playerJoin.isEnabled() && !trackedEntities.contains(pe.getId()))
                ChatUtil.info(pe.getName().getString() + " entered range!");
        }
        if (playerLeave.isEnabled()) {
            for (int id : trackedEntities)
                if (!current.contains(id)) ChatUtil.warn("A player left your range.");
        }
        trackedEntities = current;
        timer.reset();
    }
}
