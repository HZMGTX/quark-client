package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.TimerUtil;

import java.util.ArrayList;
import java.util.List;

public class ServerJoinHelper extends Module {

    private final IntSetting    delay   = register(new IntSetting("Delay", "Seconds after join before sending commands", 3, 0, 30));
    private final StringSetting cmd1    = register(new StringSetting("Command 1", "First command/message (blank to skip)", "/spawn"));
    private final StringSetting cmd2    = register(new StringSetting("Command 2", "Second command/message (blank to skip)", ""));
    private final StringSetting cmd3    = register(new StringSetting("Command 3", "Third command/message (blank to skip)", ""));
    private final IntSetting    cmdGap  = register(new IntSetting("Command Gap", "Milliseconds between each command", 500, 0, 5000));
    private final BoolSetting   onlyOnce = register(new BoolSetting("Once Per Session", "Only run once per game session (not on every server join)", false));

    private enum Phase { WAITING, SENDING, DONE }

    private Phase phase = Phase.DONE;
    private final TimerUtil delayTimer = new TimerUtil();
    private final TimerUtil gapTimer   = new TimerUtil();
    private int cmdIndex = 0;
    private List<String> cmds = new ArrayList<>();
    private boolean wasConnected = false;
    private boolean firedThisSession = false;

    public ServerJoinHelper() {
        super("ServerJoinHelper", "Auto-runs commands/messages on server join", Category.MISC);
    }

    @Override
    public void onEnable() {
        wasConnected = mc.player != null;
        phase = Phase.DONE;
        firedThisSession = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        boolean connected = mc.player != null && mc.getNetworkHandler() != null;

        // Detect fresh join (was disconnected, now connected)
        if (!wasConnected && connected) {
            wasConnected = true;
            if (!onlyOnce.isEnabled() || !firedThisSession) {
                startSequence();
            }
        }

        if (!connected) {
            wasConnected = false;
            if (phase != Phase.DONE) phase = Phase.DONE;
            return;
        }

        if (phase == Phase.WAITING) {
            if (delayTimer.hasReached(delay.get() * 1000L)) {
                phase = Phase.SENDING;
                cmdIndex = 0;
                gapTimer.reset();
            }
        }

        if (phase == Phase.SENDING) {
            if (!gapTimer.hasReached(cmdGap.get())) return;
            if (cmdIndex >= cmds.size()) {
                phase = Phase.DONE;
                firedThisSession = true;
                return;
            }

            String cmd = cmds.get(cmdIndex);
            cmdIndex++;
            gapTimer.reset();

            if (!cmd.isBlank()) {
                if (cmd.startsWith("/")) {
                    mc.getNetworkHandler().sendChatCommand(cmd.substring(1));
                } else {
                    mc.getNetworkHandler().sendChatMessage(cmd);
                }
            }
        }
    }

    private void startSequence() {
        cmds = new ArrayList<>();
        if (!cmd1.get().isBlank()) cmds.add(cmd1.get());
        if (!cmd2.get().isBlank()) cmds.add(cmd2.get());
        if (!cmd3.get().isBlank()) cmds.add(cmd3.get());
        if (cmds.isEmpty()) return;
        phase = Phase.WAITING;
        delayTimer.reset();
    }
}
