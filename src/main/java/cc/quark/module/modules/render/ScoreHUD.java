package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class ScoreHUD extends Module {

    private final IntSetting  posX      = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting  posY      = register(new IntSetting("Y", "HUD Y position", 60, 0, 3000));
    private final IntSetting  maxRows   = register(new IntSetting("Max Rows", "Maximum scoreboard rows shown", 15, 1, 20));
    private final BoolSetting bgBox     = register(new BoolSetting("Background", "Draw dark background box", true));
    private final BoolSetting showTitle = register(new BoolSetting("Show Title",  "Show the objective display name", true));
    private final BoolSetting rightAlign = register(new BoolSetting("Right Align", "Anchor to right side of screen", false));

    public ScoreHUD() {
        super("ScoreHUD", "Shows scoreboard objectives on a custom HUD overlay", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        Scoreboard scoreboard = mc.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return;

        DrawContext ctx = event.getDrawContext();
        int lh = mc.textRenderer.fontHeight + 1;

        // Collect entries
        Collection<ScoreHolder> holders = scoreboard.getKnownScoreHolders();
        List<ReadableScoreboardScore> scores = new ArrayList<>();
        for (ScoreHolder holder : holders) {
            ReadableScoreboardScore score = scoreboard.getScore(holder, objective);
            if (score != null) scores.add(score);
        }
        scores.sort(Comparator.comparingInt(ReadableScoreboardScore::getScore).reversed());

        int count = Math.min(scores.size(), maxRows.get());
        if (count == 0) return;

        int titleLines = showTitle.isEnabled() ? 1 : 0;
        int totalLines = count + titleLines;

        // Determine max width
        int maxW = 0;
        if (showTitle.isEnabled()) {
            maxW = Math.max(maxW, mc.textRenderer.getWidth(objective.getDisplayName()));
        }
        for (int i = 0; i < count; i++) {
            String line = buildLine(scores.get(i));
            maxW = Math.max(maxW, mc.textRenderer.getWidth(line));
        }

        int sw = mc.getWindow().getScaledWidth();
        int x = rightAlign.isEnabled() ? sw - maxW - posX.get() - 4 : posX.get();
        int y = posY.get();

        if (bgBox.isEnabled()) {
            ctx.fill(x - 2, y - 2, x + maxW + 4, y + totalLines * lh + 2, 0x88000000);
        }

        if (showTitle.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, objective.getDisplayName(), x, y, 0xFFFFFF55);
            y += lh;
        }

        for (int i = 0; i < count; i++) {
            ReadableScoreboardScore score = scores.get(i);
            String line = buildLine(score);
            int color = (score.getScoreHolder().getNameForScoreboard().equals(
                    mc.player.getName().getString())) ? 0xFFFFFF55 : 0xFFFFFFFF;
            ctx.drawTextWithShadow(mc.textRenderer, line, x, y, color);
            y += lh;
        }
    }

    private String buildLine(ReadableScoreboardScore score) {
        return String.format("§f%s §7%d",
                score.getScoreHolder().getNameForScoreboard(),
                score.getScore());
    }
}
