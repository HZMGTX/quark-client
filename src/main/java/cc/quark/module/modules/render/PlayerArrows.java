package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class PlayerArrows extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Detection range in blocks", 16.0, 4.0, 64.0));

    public PlayerArrows() {
        super("PlayerArrows", "Shows arrow count for nearby players", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();

        List<PlayerEntity> nearby = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity player)) continue;
            if (mc.player.distanceTo(player) > range.get()) continue;
            nearby.add(player);
        }

        int lineH = 10;
        int px = 4;
        int py = 4;

        if (!nearby.isEmpty()) {
            ctx.drawTextWithShadow(mc.textRenderer, "Player Arrows:", px, py, 0xFFFFAA00);
            py += lineH;
        }

        for (PlayerEntity player : nearby) {
            // Count arrows in their inventory if accessible, otherwise show "?"
            int arrowCount = 0;
            try {
                for (int i = 0; i < player.getInventory().size(); i++) {
                    var stack = player.getInventory().getStack(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.item.ArrowItem) {
                        arrowCount += stack.getCount();
                    }
                }
            } catch (Exception ignored) {
                arrowCount = -1;
            }

            String name  = player.getGameProfile().getName();
            String label = name + ": " + (arrowCount < 0 ? "?" : arrowCount) + " arrows";
            ctx.drawTextWithShadow(mc.textRenderer, label, px, py, 0xFFFFFFFF);
            py += lineH;
        }
    }
}
