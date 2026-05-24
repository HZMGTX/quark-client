package cc.quark.module.modules.render;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;

public class TimeChanger extends Module {

    private final IntSetting time = register(new IntSetting("Time", "Client-side time (0=midnight, 6000=day, 18000=sunset)", 6000, 0, 24000));

    public TimeChanger() {
        super("TimeChanger", "Changes the client-side time of day", Category.RENDER, 0);
    }

    @Override
    public void onEnable() {
        Quark.getInstance().getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        Quark.getInstance().getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null) return;
        // Client-side time override via world properties
        // Note: requires access to ClientWorld internals or mixin for full effect
    }

    public long getClientTime() {
        return time.getValue();
    }
}
