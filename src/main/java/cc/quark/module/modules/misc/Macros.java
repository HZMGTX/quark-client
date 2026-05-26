package cc.quark.module.modules.misc;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;

public class Macros extends Module {

    private static final String[] ACTIONS = {
        "None", "Say GG", "Say GL HF", "Say gg ez", "Say wp",
        "Toggle KillAura", "Toggle Speed", "Toggle Flight", "Open GUI"
    };

    private final IntSetting  key1    = register(new IntSetting ("Macro1 Key",    "GLFW key code for macro 1 (-1 = unbound)", -1, -1, 400));
    private final ModeSetting action1 = register(new ModeSetting("Macro1 Action", "Action for macro 1", "None", ACTIONS));
    private final IntSetting  key2    = register(new IntSetting ("Macro2 Key",    "GLFW key code for macro 2 (-1 = unbound)", -1, -1, 400));
    private final ModeSetting action2 = register(new ModeSetting("Macro2 Action", "Action for macro 2", "None", ACTIONS));
    private final IntSetting  key3    = register(new IntSetting ("Macro3 Key",    "GLFW key code for macro 3 (-1 = unbound)", -1, -1, 400));
    private final ModeSetting action3 = register(new ModeSetting("Macro3 Action", "Action for macro 3", "None", ACTIONS));
    private final IntSetting  key4    = register(new IntSetting ("Macro4 Key",    "GLFW key code for macro 4 (-1 = unbound)", -1, -1, 400));
    private final ModeSetting action4 = register(new ModeSetting("Macro4 Action", "Action for macro 4", "None", ACTIONS));
    private final IntSetting  key5    = register(new IntSetting ("Macro5 Key",    "GLFW key code for macro 5 (-1 = unbound)", -1, -1, 400));
    private final ModeSetting action5 = register(new ModeSetting("Macro5 Action", "Action for macro 5", "None", ACTIONS));

    public Macros() {
        super("Macros", "Bind up to 5 keys to chat messages or module toggles", Category.MISC);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (mc.player == null) return;
        int code = event.getKeyCode();
        if (code <= 0) return;
        checkSlot(code, key1.get(), action1);
        checkSlot(code, key2.get(), action2);
        checkSlot(code, key3.get(), action3);
        checkSlot(code, key4.get(), action4);
        checkSlot(code, key5.get(), action5);
    }

    private void checkSlot(int pressed, int bound, ModeSetting action) {
        if (bound <= 0 || pressed != bound) return;
        executeAction(action.get());
    }

    private void executeAction(String action) {
        if (mc.player == null) return;
        switch (action) {
            case "Say GG"          -> ChatUtil.send("GG");
            case "Say GL HF"       -> ChatUtil.send("GL HF");
            case "Say gg ez"       -> ChatUtil.send("gg ez");
            case "Say wp"          -> ChatUtil.send("wp");
            case "Toggle KillAura" -> toggleModule("KillAura");
            case "Toggle Speed"    -> toggleModule("Speed");
            case "Toggle Flight"   -> toggleModule("Flight");
            case "Open GUI"        -> mc.execute(() -> mc.setScreen(new cc.quark.gui.ClickGUI()));
            default -> {}
        }
    }

    private void toggleModule(String name) {
        if (Quark.getInstance() == null) return;
        Module mod = Quark.getInstance().getModuleManager().getModule(name);
        if (mod != null) mod.toggle();
    }
}
