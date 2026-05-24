package com.ghostclient.module.modules.render;

import com.ghostclient.GhostClient;
import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.IntSetting;

public class TimeChanger extends Module {

    private final IntSetting time = register(new IntSetting("Time", "Client-side time (0=midnight, 6000=day, 18000=sunset)", 6000, 0, 24000));

    public TimeChanger() {
        super("TimeChanger", "Changes the client-side time of day", Category.RENDER, 0);
    }

    @Override
    public void onEnable() {
        GhostClient.getInstance().getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        GhostClient.getInstance().getEventBus().unsubscribe(this);
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
