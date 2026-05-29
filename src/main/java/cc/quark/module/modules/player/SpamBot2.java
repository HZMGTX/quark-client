package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;

import java.util.List;
import java.util.Random;

public class SpamBot2 extends Module {

    private final IntSetting minDelay = register(new IntSetting(
            "MinDelay", "Minimum seconds between messages", 5, 1, 120));
    private final IntSetting maxDelay = register(new IntSetting(
            "MaxDelay", "Maximum seconds between messages", 15, 1, 300));

    private final List<String> spamMessages = List.of(
            "Quark > all",
            "best client no cap",
            "Quark client is fire",
            "quark.cc best client 2025",
            "lol get quark"
    );

    private final TimerUtil timer = new TimerUtil();
    private final Random rand = new Random();
    private long nextDelay = 10_000L;
    private int sentCount = 0;

    public SpamBot2() {
        super("SpamBot2", "Spams messages from a list with random delays between sends", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
        sentCount = 0;
        randomizeDelay();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!timer.hasReached(nextDelay)) return;
        timer.reset();
        randomizeDelay();

        String msg = spamMessages.get(rand.nextInt(spamMessages.size()));
        ChatUtil.send(msg);
        sentCount++;
    }

    private void randomizeDelay() {
        int lo = minDelay.get();
        int hi = Math.max(lo + 1, maxDelay.get());
        nextDelay = (lo + rand.nextInt(hi - lo)) * 1000L;
    }

    @Override
    public String getSuffix() {
        return "sent " + sentCount;
    }
}
