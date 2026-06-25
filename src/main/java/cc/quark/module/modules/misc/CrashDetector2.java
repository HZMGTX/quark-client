package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;

public class CrashDetector2 extends Module {
    private final IntSetting lagThreshold = register(new IntSetting("LagThreshold", "Ticks without update to consider lag", 20, 5, 100));
    private final BoolSetting autoDisconnect = register(new BoolSetting("AutoDisconnect", "Auto disconnect on detected crash", false));
    private long lastTickTime = 0;
    private int lagTicks = 0;

    public CrashDetector2() { super("CrashDetector2", "Detects server lag and crash attempts", Category.MISC); }

    @EventHandler
    public void onTick(EventTick event) {
        long now = System.currentTimeMillis();
        long delta = now - lastTickTime;
        lastTickTime = now;
        if (delta > 200) {
            lagTicks++;
            if (lagTicks >= lagThreshold.getValue()) {
                ChatUtil.warn("Server lag detected! Delta: " + delta + "ms");
                if (autoDisconnect.getValue() && mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().getConnection().disconnect(net.minecraft.text.Text.literal("Quark: Crash detected"));
                }
                lagTicks = 0;
            }
        } else {
            lagTicks = 0;
        }
    }
}
