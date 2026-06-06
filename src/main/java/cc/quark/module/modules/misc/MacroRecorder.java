package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;

import java.util.ArrayList;
import java.util.List;

public class MacroRecorder extends Module {

    private final IntSetting  recordKey  = register(new IntSetting("Record Key", "GLFW key code to start/stop recording", 82 /* R */,   0, 350));
    private final IntSetting  playKey    = register(new IntSetting("Play Key",   "GLFW key code to replay the macro",      80 /* P */,   0, 350));
    private final IntSetting  speed      = register(new IntSetting("Speed",      "Replay speed multiplier (1 = normal)",    1, 1, 10));
    private final BoolSetting loop       = register(new BoolSetting("Loop",      "Continuously replay macro until stopped", false));
    private final BoolSetting showStatus = register(new BoolSetting("Status HUD","Show recording/playback status",          true));

    private enum State { IDLE, RECORDING, PLAYING }

    private static class KeyEvent {
        final int key;
        final long timestamp;
        KeyEvent(int key, long timestamp) {
            this.key = key; this.timestamp = timestamp;
        }
    }

    private State state = State.IDLE;
    private final List<KeyEvent> recorded = new ArrayList<>();
    private long recordStart = 0;

    // Playback state
    private int playIndex = 0;
    private long playStart = 0;

    public MacroRecorder() {
        super("MacroRecorder", "Records and replays key press sequences as macros", Category.MISC);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
        state = State.IDLE;
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        state = State.IDLE;
    }

    @EventHandler
    public void onKey(EventKey event) {
        int key = event.getKeyCode();

        // Record toggle
        if (key == recordKey.get()) {
            if (state == State.RECORDING) {
                state = State.IDLE;
                notify("Macro recorded: " + recorded.size() + " events");
            } else if (state == State.IDLE) {
                recorded.clear();
                state = State.RECORDING;
                recordStart = System.currentTimeMillis();
                notify("Recording...");
            }
            return;
        }

        // Play toggle
        if (key == playKey.get()) {
            if (state == State.PLAYING) {
                state = State.IDLE;
                notify("Playback stopped");
            } else if (state == State.IDLE && !recorded.isEmpty()) {
                state = State.PLAYING;
                playIndex = 0;
                playStart = System.currentTimeMillis();
                notify("Playing macro (" + recorded.size() + " events)");
            }
            return;
        }

        // Capture key during recording
        if (state == State.RECORDING) {
            long ts = System.currentTimeMillis() - recordStart;
            recorded.add(new KeyEvent(key, ts));
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (state != State.PLAYING || recorded.isEmpty()) return;
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        long elapsed = (now - playStart) * speed.get();

        while (playIndex < recorded.size()) {
            KeyEvent ke = recorded.get(playIndex);
            if (elapsed >= ke.timestamp) {
                // Simulate key press via GLFW
                long window = mc.getWindow().getHandle();
                mc.keyboard.onKey(window, ke.key, 0, 1 /* press */, 0);
                playIndex++;
            } else {
                break;
            }
        }

        if (playIndex >= recorded.size()) {
            if (loop.isEnabled()) {
                playIndex = 0;
                playStart = System.currentTimeMillis();
            } else {
                state = State.IDLE;
                notify("Macro finished");
            }
        }
    }

    private void notify(String msg) {
        if (showStatus.isEnabled() && mc.inGameHud != null) {
            mc.inGameHud.getChatHud().addMessage(
                    net.minecraft.text.Text.literal("§e[MacroRecorder] §f" + msg));
        }
    }
}
