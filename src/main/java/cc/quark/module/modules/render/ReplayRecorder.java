package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;

import java.util.ArrayList;
import java.util.List;

public class ReplayRecorder extends Module {
    private final IntSetting maxLength = register(new IntSetting("MaxSeconds", "Max recording length (seconds)", 30, 5, 120));
    private final BoolSetting autoReplay = register(new BoolSetting("AutoReplay", "Replay automatically on death", false));
    private final List<double[]> frames = new ArrayList<>();

    public ReplayRecorder() { super("ReplayRecorder", "Records and replays player movements", Category.RENDER); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (frames.size() > maxLength.getValue() * 20) frames.remove(0);
        frames.add(new double[]{ mc.player.getX(), mc.player.getY(), mc.player.getZ(),
            mc.player.getYaw(), mc.player.getPitch() });
    }

    @Override
    public void onDisable() {
        ChatUtil.info("Recorded " + frames.size() + " frames.");
        frames.clear();
    }
}
