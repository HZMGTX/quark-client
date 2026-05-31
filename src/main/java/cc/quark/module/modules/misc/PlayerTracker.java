package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerTracker extends Module {

    private final BoolSetting showHUD   = register(new BoolSetting("ShowHUD",   "Show last-known positions on screen", true));
    private final IntSetting  maxTrack  = register(new IntSetting("MaxTrack",   "Max players to track",                10, 1, 50));
    private final IntSetting  posX      = register(new IntSetting("X",          "HUD X position",                     4,  0, 500));
    private final IntSetting  posY      = register(new IntSetting("Y",          "HUD Y position",                     100, 0, 500));

    private final Map<String, Vec3d> lastPositions = new LinkedHashMap<>();

    public PlayerTracker() {
        super("PlayerTracker", "Tracks nearby player positions over time and shows last-known locations", Category.MISC);
    }

    @Override
    public void onDisable() {
        lastPositions.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player) continue;
            if (lastPositions.size() >= maxTrack.get() && !lastPositions.containsKey(p.getName().getString())) continue;
            lastPositions.put(p.getName().getString(), p.getPos());
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showHUD.isEnabled() || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int x = posX.get();
        int y = posY.get();
        int lh = mc.textRenderer.fontHeight + 2;

        ctx.drawTextWithShadow(mc.textRenderer, "§e§lTracked Players", x, y, 0xFFFFFFFF);
        y += lh;

        int count = 0;
        for (Map.Entry<String, Vec3d> entry : lastPositions.entrySet()) {
            if (count++ >= 8) break;
            Vec3d pos = entry.getValue();
            String text = String.format("§f%s §7(%.0f, %.0f, %.0f)", entry.getKey(), pos.x, pos.y, pos.z);
            ctx.drawTextWithShadow(mc.textRenderer, text, x, y, 0xFFFFFFFF);
            y += lh;
        }
    }
}
