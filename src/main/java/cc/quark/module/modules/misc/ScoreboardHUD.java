package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScoreboardHUD extends Module {

    private final IntSetting posX = register(new IntSetting(
            "X", "HUD X position", 4, 0, 500));
    private final IntSetting posY = register(new IntSetting(
            "Y", "HUD Y position", 34, 0, 500));

    public ScoreboardHUD() {
        super("ScoreboardHUD", "Displays the sidebar scoreboard as a HUD overlay", Category.MISC);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        // Implementation commented out for 1.21.1
    }
}
