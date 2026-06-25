package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public class EntityCounter extends Module {

    private final BoolSetting mobs = register(new BoolSetting(
            "Mobs", "Count nearby mobs", true));

    private final BoolSetting players = register(new BoolSetting(
            "Players", "Count nearby players", true));

    private final IntSetting range = register(new IntSetting(
            "Range", "Counting range in blocks", 64, 8, 256));

    public EntityCounter() {
        super("EntityCounter", "Shows entity counts on HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        DrawContext ctx = event.getDrawContext();
        int r = range.get();
        int mobCount = 0;
        int playerCount = 0;

        for (var entity : mc.world.getEntities()) {
            if (mc.player.distanceTo(entity) > r) continue;
            if (mobs.isEnabled() && entity instanceof MobEntity) mobCount++;
            if (players.isEnabled() && entity instanceof PlayerEntity p && p != mc.player) playerCount++;
        }

        int y = 2;
        if (mobs.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, "Mobs: " + mobCount, 2, y, 0xFFFF5555);
            y += 10;
        }
        if (players.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, "Players: " + playerCount, 2, y, 0xFF55FFFF);
        }
    }
}
