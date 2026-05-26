package cc.quark.command.commands;

import cc.quark.command.Command;
import cc.quark.gui.EditHudScreen;
import net.minecraft.client.MinecraftClient;

public class HudCommand extends Command {

    public HudCommand() {
        super("hud", "Opens the HUD editor to drag elements.", "hud");
    }

    @Override
    public void execute(String[] args) {
        // Must be scheduled on next tick since we are processing a chat packet
        MinecraftClient.getInstance().send(() -> {
            MinecraftClient.getInstance().setScreen(new EditHudScreen());
        });
        cc.quark.util.ChatUtil.info("Opening HUD Editor...");
    }
}
