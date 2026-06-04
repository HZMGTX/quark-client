package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

public class PortalFinder extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Search radius for nether portals (blocks)", 64, 16, 256));
    private final BoolSetting showCoords = register(new BoolSetting(
            "ShowCoords", "Display portal coordinates on screen", true));

    private final TimerUtil scanTimer = new TimerUtil();
    private BlockPos nearestPortal = null;
    private double nearestDist = Double.MAX_VALUE;

    public PortalFinder() {
        super("PortalFinder", "Finds the nearest nether portal in the world", Category.WORLD);
    }

    @Override
    public void onEnable() {
        nearestPortal = null;
        nearestDist = Double.MAX_VALUE;
        scanTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!scanTimer.hasReached(1000)) return;
        scanTimer.reset();

        int r = range.get();
        BlockPos center = mc.player.getBlockPos();
        nearestPortal = null;
        nearestDist = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.iterate(
                center.add(-r, -r / 2, -r),
                center.add(r, r / 2, r))) {
            if (!mc.world.getBlockState(pos).isOf(Blocks.NETHER_PORTAL)) continue;
            // Only count one block per portal frame to avoid duplicates
            // Check that the block below is not also a portal (bottom of portal)
            if (mc.world.getBlockState(pos.down()).isOf(Blocks.NETHER_PORTAL)) continue;

            double dist = mc.player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
            if (dist < nearestDist) {
                nearestDist = dist;
                nearestPortal = pos.toImmutable();
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showCoords.isEnabled()) return;
        if (nearestPortal == null) return;

        DrawContext ctx = event.getDrawContext();
        int screenW = mc.getWindow().getScaledWidth();
        int y = 30;

        int dist = (int) Math.sqrt(nearestDist);
        String line1 = "Portal: " + nearestPortal.getX() + " " + nearestPortal.getY() + " " + nearestPortal.getZ();
        String line2 = "Distance: " + dist + "m";

        int w1 = mc.textRenderer.getWidth(line1);
        int w2 = mc.textRenderer.getWidth(line2);
        int x1 = (screenW - w1) / 2;
        int x2 = (screenW - w2) / 2;

        ctx.drawTextWithShadow(mc.textRenderer, line1, x1, y, 0xFF9933FF);
        ctx.drawTextWithShadow(mc.textRenderer, line2, x2, y + 10, 0xFFCC99FF);
    }
}
