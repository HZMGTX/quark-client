package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class WitchHunt extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Range to detect witch entities and huts", 128, 16, 256));
    private final BoolSetting showHUD = register(new BoolSetting(
            "ShowHUD", "Display witch tracking info on the HUD", true));

    private final TimerUtil scanTimer = new TimerUtil();
    private final List<BlockPos> witchPositions = new ArrayList<>();
    private int witchCount = 0;

    public WitchHunt() {
        super("WitchHunt", "Tracks witch entities nearby and estimates witch hut spawn positions", Category.WORLD);
    }

    @Override
    public void onEnable() {
        witchPositions.clear();
        witchCount = 0;
        scanTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!scanTimer.hasReached(1000)) return;
        scanTimer.reset();

        double rangeSq = (double) range.get() * range.get();
        witchPositions.clear();
        witchCount = 0;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof WitchEntity witch)) continue;
            double distSq = mc.player.squaredDistanceTo(witch);
            if (distSq > rangeSq) continue;
            witchCount++;
            witchPositions.add(witch.getBlockPos().toImmutable());
        }

        if (witchCount > 0 && showHUD.isEnabled()) {
            ChatUtil.info("[WitchHunt] " + witchCount + " witch(es) detected in range.");
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showHUD.isEnabled()) return;
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int y = 70;

        ctx.drawTextWithShadow(mc.textRenderer,
                "Witches: " + witchCount, 4, y, 0xFF9900AA);

        for (int i = 0; i < witchPositions.size() && i < 5; i++) {
            BlockPos pos = witchPositions.get(i);
            int dist = (int) mc.player.getPos().distanceTo(
                    new net.minecraft.util.math.Vec3d(pos.getX(), pos.getY(), pos.getZ()));
            String line = "  " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " (" + dist + "m)";
            ctx.drawTextWithShadow(mc.textRenderer, line, 4, y + 10 + i * 10, 0xFFCC44DD);
        }
    }
}
