package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;

import java.util.Arrays;
import java.util.List;

public class AutoGG extends Module {

    private final ModeSetting message = register(new ModeSetting(
            "Message", "Message to send at game end",
            "gg", "gg", "GG!", "Good Game!", "gg ez"));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks to wait before sending (0-100)", 20, 0, 100));

    private final BoolSetting winOnly = register(new BoolSetting(
            "Win Only", "Only send GG when you won", false));

    private static final List<String> WIN_PATTERNS = Arrays.asList(
            "you won the game", "you win!", "winners:", "1st place", "you placed 1st"
    );

    private static final List<String> END_PATTERNS = Arrays.asList(
            "game over!", "you lost!", "final kill", "game over",
            "the game has ended", "match ended", "round over"
    );

    private int pendingTicks = -1;
    private boolean pendingWin = false;

    public AutoGG() {
        super("AutoGG", "Automatically says gg at the end of a game", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        pendingTicks = -1;
        pendingWin   = false;
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String lower = event.getMessage().toLowerCase();

        boolean isWin = WIN_PATTERNS.stream().anyMatch(lower::contains);
        boolean isEnd = END_PATTERNS.stream().anyMatch(lower::contains) || isWin;

        if (!isEnd) return;

        if (winOnly.isEnabled() && !isWin) return;

        pendingTicks = delay.get();
        pendingWin   = isWin;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || pendingTicks < 0) return;

        if (pendingTicks == 0) {
            ChatUtil.send(message.get());
            pendingTicks = -1;
        } else {
            pendingTicks--;
        }
    }
}
