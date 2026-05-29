package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;

import java.util.List;
import java.util.Random;

public class AutoMessage extends Module {

    private final IntSetting minDelay = register(new IntSetting(
            "MinDelay", "Minimum seconds between messages", 20, 5, 300));
    private final IntSetting maxDelay = register(new IntSetting(
            "MaxDelay", "Maximum seconds between messages", 60, 5, 600));
    private final BoolSetting repeat = register(new BoolSetting(
            "Repeat", "Cycle through all messages repeatedly", true));

    private final List<String> messages = List.of(
            "Powered by Quark client!",
            "Use Quark for the best experience.",
            "GG everyone!"
    );

    private final TimerUtil timer = new TimerUtil();
    private final Random rand = new Random();
    private int msgIndex = 0;
    private long nextDelay = 30_000L;

    public AutoMessage() {
        super("AutoMessage", "Sends messages from a list on a random timer interval", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
        msgIndex = 0;
        randomizeDelay();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!timer.hasReached(nextDelay)) return;
        timer.reset();
        randomizeDelay();

        if (messages.isEmpty()) return;
        if (msgIndex >= messages.size()) {
            if (!repeat.isEnabled()) return;
            msgIndex = 0;
        }
        ChatUtil.send(messages.get(msgIndex));
        msgIndex++;
    }

    private void randomizeDelay() {
        int lo = minDelay.get();
        int hi = Math.max(lo + 1, maxDelay.get());
        nextDelay = (lo + rand.nextInt(hi - lo)) * 1000L;
    }

    @Override
    public String getSuffix() {
        return (msgIndex + 1) + "/" + messages.size();
    }
}
