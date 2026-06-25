package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
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

public class ScoreboardUtils extends Module {

    private final IntSetting  posX      = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting  posY      = register(new IntSetting("Y", "HUD Y position", 100, 0, 3000));
    private final IntSetting  maxRows   = register(new IntSetting("Max Rows", "Maximum entries displayed", 15, 1, 20));
    private final BoolSetting showTitle = register(new BoolSetting("Show Title",  "Show objective name as header", true));
    private final BoolSetting bgBox     = register(new BoolSetting("Background",  "Draw dark background", true));
    private final ModeSetting sortMode  = register(new ModeSetting("Sort", "Sorting mode",
            "Descending", "Descending", "Ascending", "None"));
    private final BoolSetting hideVanilla = register(new BoolSetting("Hide Vanilla Scoreboard", "Hide the default sidebar scoreboard", true));

    private final List<String> lines = new ArrayList<>();

    public ScoreboardUtils() {
        super("ScoreboardUtils", "Enhanced scoreboard reading, sorting, and custom display", Category.MISC);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        lines.clear();
        Scoreboard scoreboard = mc.world.getScoreboard();
        ScoreboardObjective obj = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (obj == null) return;

        Collection<ScoreHolder> holders = scoreboard.getKnownScoreHolders();
        List<ReadableScoreboardScore> scores = new ArrayList<>();
        for (ScoreHolder holder : holders) {
            ReadableScoreboardScore score = scoreboard.getScore(holder, obj);
            if (score != null) scores.add(score);
        }

        String sort = sortMode.get();
        if (sort.equals("Descending")) {
            scores.sort(Comparator.comparingInt(ReadableScoreboardScore::getScore).reversed());
        } else if (sort.equals("Ascending")) {
            scores.sort(Comparator.comparingInt(ReadableScoreboardScore::getScore));
        }

        if (showTitle.isEnabled()) {
            lines.add("§e" + obj.getDisplayName().getString());
        }

        int count = Math.min(scores.size(), maxRows.get());
        for (int i = 0; i < count; i++) {
            ReadableScoreboardScore s = scores.get(i);
            String name = s.getScoreHolder().getNameForScoreboard();
            boolean self = mc.player != null && name.equals(mc.player.getName().getString());
            String prefix = self ? "§6" : "§f";
            lines.add(prefix + name + " §7" + s.getScore());
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (lines.isEmpty()) return;

        DrawContext ctx = event.getDrawContext();
        int x = posX.get(), y = posY.get();
        int lh = mc.textRenderer.fontHeight + 1;

        if (bgBox.isEnabled()) {
            int maxW = lines.stream().mapToInt(l -> mc.textRenderer.getWidth(l)).max().orElse(0);
            ctx.fill(x - 2, y - 2, x + maxW + 4, y + lines.size() * lh + 2, 0x88000000);
        }

        for (String line : lines) {
            ctx.drawTextWithShadow(mc.textRenderer, line, x, y, 0xFFFFFFFF);
            y += lh;
        }
    }

    /** Called externally to get the current sorted scoreboard lines (utility method). */
    public List<String> getLines() {
        return new ArrayList<>(lines);
    }
}
