package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class WorldEditor extends Module {
    private final ModeSetting tool = register(new ModeSetting("Tool", "WorldEdit command", "Fill", "Fill", "Replace", "Set"));
    private final StringSetting block1 = register(new StringSetting("Block", "Block to place", "minecraft:stone"));
    private final StringSetting block2 = register(new StringSetting("Replace With", "Replacement block", "minecraft:air"));
    private final IntSetting radius = register(new IntSetting("Radius", "Edit radius", 5, 1, 50));
    private boolean executed = false;

    public WorldEditor() { super("WorldEditor", "Sends WorldEdit commands for bulk editing", Category.STAFF); }
    @Override public void onEnable() { executed = false; }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || executed) return;
        String cmd = switch (tool.get()) {
            case "Replace" -> "//replace " + block1.get() + " " + block2.get();
            case "Set" -> "//set " + block1.get();
            default -> "//fill " + block1.get() + " " + radius.get();
        };
        mc.player.networkHandler.sendChatCommand(cmd.replaceFirst("/", ""));
        ChatUtil.info("WorldEdit: " + cmd);
        executed = true;
        disable();
    }
}
