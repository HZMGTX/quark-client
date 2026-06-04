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
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class DungeonFinder extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Block range to scan for spawners/dungeons", 32, 8, 64));
    private final BoolSetting showESP = register(new BoolSetting(
            "ShowESP", "Show dungeon locations on the 2D HUD", true));

    private final TimerUtil scanTimer = new TimerUtil();
    private final List<BlockPos> foundSpawners = new ArrayList<>();

    public DungeonFinder() {
        super("DungeonFinder", "Detects nearby dungeons by monster spawners and mossy cobblestone", Category.WORLD);
    }

    @Override
    public void onEnable() {
        foundSpawners.clear();
        scanTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!scanTimer.hasReached(2000)) return;
        scanTimer.reset();

        int r = range.get();
        BlockPos center = mc.player.getBlockPos();
        foundSpawners.clear();

        for (BlockPos pos : BlockPos.iterate(
                center.add(-r, -r, -r),
                center.add(r, r, r))) {
            // Check for spawner block entities
            var be = mc.world.getBlockEntity(pos);
            if (!(be instanceof MobSpawnerBlockEntity)) continue;

            // Confirm nearby mossy cobblestone (dungeon signature)
            int mossy = 0;
            for (BlockPos near : BlockPos.iterate(pos.add(-5, -2, -5), pos.add(5, 2, 5))) {
                if (mc.world.getBlockState(near).isOf(Blocks.MOSSY_COBBLESTONE)) mossy++;
            }
            if (mossy >= 4) {
                foundSpawners.add(pos.toImmutable());
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showESP.isEnabled() || foundSpawners.isEmpty()) return;
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int screenW = mc.getWindow().getScaledWidth();
        int y = 50;

        ctx.drawTextWithShadow(mc.textRenderer,
                "Dungeons: " + foundSpawners.size(), 4, y, 0xFFFF6600);

        for (int i = 0; i < foundSpawners.size() && i < 5; i++) {
            BlockPos pos = foundSpawners.get(i);
            int dist = (int) mc.player.getPos().distanceTo(
                    new net.minecraft.util.math.Vec3d(pos.getX(), pos.getY(), pos.getZ()));
            String line = "  Spawner: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
                    + " (" + dist + "m)";
            ctx.drawTextWithShadow(mc.textRenderer, line, 4, y + 10 + i * 10, 0xFFFFAA55);
        }
    }
}
