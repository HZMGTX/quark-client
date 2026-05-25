package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.ChatUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class DeathCoords extends Module {

    private final List<BlockPos> deathPositions = new ArrayList<>();
    private boolean wasDead = false;

    public DeathCoords() {
        super("DeathCoords", "Records your death coordinates and displays them on the HUD", Category.RENDER);
    }

    @Override
    public void onEnable() {
        deathPositions.clear();
        wasDead = false;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        boolean isDead = mc.player.isDead() || mc.player.getHealth() <= 0;
        if (isDead && !wasDead) {
            BlockPos pos = mc.player.getBlockPos();
            deathPositions.add(0, pos);
            if (deathPositions.size() > 10) deathPositions.remove(deathPositions.size() - 1);
            ChatUtil.info("Death at: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        }
        wasDead = isDead;

        if (deathPositions.isEmpty()) return;

        DrawContext ctx = event.getDrawContext();
        int y = mc.getWindow().getScaledHeight() - 15 - deathPositions.size() * 10;

        ctx.drawTextWithShadow(mc.textRenderer, "Death Coords:", 5, y, 0xFFFF5555);
        y += 10;
        for (int i = 0; i < deathPositions.size(); i++) {
            BlockPos pos = deathPositions.get(i);
            String text = (i + 1) + ". " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
            ctx.drawTextWithShadow(mc.textRenderer, text, 5, y, 0xFFFFFFFF);
            y += 10;
        }
    }
}
