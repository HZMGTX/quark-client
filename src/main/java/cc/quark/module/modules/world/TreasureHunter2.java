package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class TreasureHunter2 extends Module {

    private BlockPos treasureTarget = null;

    public TreasureHunter2() {
        super("TreasureHunter2", "Finds and highlights buried treasure based on map; shows directional arrow", Category.WORLD);
    }

    @Override
    public void onEnable() {
        treasureTarget = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (!stack.isOf(Items.FILLED_MAP)) continue;
            //? if mc >= "1.20.5" {
            Integer mapId = stack.get(net.minecraft.component.DataComponentTypes.MAP_ID);
            //?} else {
            /*Integer mapId = net.minecraft.item.FilledMapItem.getMapId(stack);*/
            //?}
            if (mapId == null) continue;
            MapState state = mc.world.getMapState(net.minecraft.item.FilledMapItem.getMapName(mapId));
            if (state == null) continue;

            for (var icon : state.getIcons()) {
                if (icon.type() == net.minecraft.item.map.MapIcon.Type.RED_X) {
                    double worldX = icon.x() / 2.0;
                    double worldZ = icon.z() / 2.0;
                    treasureTarget = new BlockPos((int) worldX, 0, (int) worldZ);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || treasureTarget == null) return;
        var context = event.getDrawContext();

        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();
        int cx = screenW / 2;
        int cy = screenH / 2;

        Vec3d playerPos = mc.player.getPos();
        double dx = treasureTarget.getX() - playerPos.x;
        double dz = treasureTarget.getZ() - playerPos.z;
        double dist = Math.sqrt(dx * dx + dz * dz);

        String text = String.format("Treasure: %.1fm", dist);
        context.drawText(mc.textRenderer, text, cx - mc.textRenderer.getWidth(text) / 2, cy - 20, 0xFFFFD700, true);

        if (dist > 1) {
            double angle = Math.atan2(dz, dx) - Math.toRadians(mc.player.getYaw() + 90);
            int arrowX = cx + (int) (Math.cos(angle) * 30);
            int arrowY = cy + (int) (Math.sin(angle) * 30);
            context.fill(arrowX - 2, arrowY - 2, arrowX + 3, arrowY + 3, 0xFFFFD700);
        }
    }
}
