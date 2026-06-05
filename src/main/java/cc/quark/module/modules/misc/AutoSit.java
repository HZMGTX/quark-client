package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;

public class AutoSit extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "How to sit",
            "Command", "Command", "Sneak"));

    private final BoolSetting standOnMove = register(new BoolSetting(
            "Stand On Move", "Unsit automatically when the player starts moving", true));

    private boolean sitting = false;

    public AutoSit() {
        super("AutoSit", "Makes the player sit using /sit or a continuous sneak trick", Category.MISC);
    }

    @Override
    public void onEnable() {
        sitting = false;
        sit();
    }

    @Override
    public void onDisable() {
        if (sitting && mode.is("Command") && mc.player != null && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendChatMessage("/sit");  // toggle off on servers that support it
        }
        if (mode.is("Sneak") && mc.player != null) {
            mc.options.sneakKey.setPressed(false);
        }
        sitting = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean isMoving = mc.player.getVelocity().horizontalLength() > 0.01;

        if (standOnMove.isEnabled() && sitting && isMoving) {
            unsit();
            return;
        }

        if (!sitting) {
            sit();
        }

        if (mode.is("Sneak")) {
            // Hold sneak every tick to stay seated (works on vanilla/some servers)
            mc.options.sneakKey.setPressed(true);
        }
    }

    private void sit() {
        if (mc.player == null) return;
        if (mode.is("Command")) {
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendChatMessage("/sit");
            }
        } else {
            mc.options.sneakKey.setPressed(true);
        }
        sitting = true;
    }

    private void unsit() {
        if (mc.player == null) return;
        if (mode.is("Sneak")) {
            mc.options.sneakKey.setPressed(false);
        }
        sitting = false;
        ChatUtil.info("AutoSit: stood up because you moved.");
    }
}
