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
        if (mc.world == null || mc.player == null) return;

        Scoreboard scoreboard = mc.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return;

        DrawContext ctx = event.getDrawContext();
        int x = posX.get();
        int y = posY.get();
        int lh = mc.textRenderer.fontHeight + 2;

        // Draw objective name
        String title = objective.getDisplayName().getString();
        ctx.drawTextWithShadow(mc.textRenderer, "§e§l" + title, x, y, 0xFFFFFFFF);
        y += lh;

        // Get scored entries and sort by score descending
        Map<ScoreHolder, ScoreboardScore> scoreMap = scoreboard.getScoreboardEntries(objective);
        List<Map.Entry<ScoreHolder, ScoreboardScore>> sorted = new ArrayList<>(scoreMap.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue().getScore(), a.getValue().getScore()));

        int count = 0;
        for (Map.Entry<ScoreHolder, ScoreboardScore> entry : sorted) {
            if (count >= 10) break;
            String name = entry.getKey().getNameForScoreboard();
            int score = entry.getValue().getScore();
            ctx.drawTextWithShadow(mc.textRenderer, name + ": §a" + score, x, y, 0xFFFFFFFF);
            y += lh;
            count++;
        }
    }
}
