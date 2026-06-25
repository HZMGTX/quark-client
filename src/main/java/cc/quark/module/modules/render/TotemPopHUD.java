package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;

import java.util.HashMap;
import java.util.Map;

public class TotemPopHUD extends Module {

    private final BoolSetting self   = register(new BoolSetting("Self",   "Count your own totem pops",          true));
    private final BoolSetting others = register(new BoolSetting("Others", "Count totem pops for other players", true));

    // Player UUID -> pop count
    private final Map<String, Integer> popCounts     = new HashMap<>();
    // Previous offhand item to detect totem change
    private final Map<String, Boolean> hadTotem      = new HashMap<>();

    public TotemPopHUD() {
        super("TotemPopHUD", "Counts totem pops for you and others", Category.RENDER);
    }

    @Override
    public void onEnable() {
        popCounts.clear();
        hadTotem.clear();
    }

    @Override
    public void onDisable() {
        popCounts.clear();
        hadTotem.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;

            boolean isSelf = (player == mc.player);
            if (isSelf && !self.isEnabled()) continue;
            if (!isSelf && !others.isEnabled()) continue;

            String uuid = player.getUuidAsString();
            boolean hasTotemNow = player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
            Boolean hadBefore   = hadTotem.getOrDefault(uuid, hasTotemNow);

            // If the player had a totem last tick but doesn't now, they popped
            if (hadBefore && !hasTotemNow) {
                popCounts.merge(uuid, 1, Integer::sum);
            }

            hadTotem.put(uuid, hasTotemNow);
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();

        int px = 4;
        int py = 4;
        int lineH = 10;

        boolean hasAny = false;
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            boolean isSelf = (player == mc.player);
            if (isSelf && !self.isEnabled()) continue;
            if (!isSelf && !others.isEnabled()) continue;

            String uuid  = player.getUuidAsString();
            int pops     = popCounts.getOrDefault(uuid, 0);
            if (pops == 0 && !isSelf) continue;

            if (!hasAny) {
                ctx.drawTextWithShadow(mc.textRenderer, "Totem Pops:", px, py, 0xFFFFAA00);
                py += lineH;
                hasAny = true;
            }

            String name  = player.getGameProfile().getName();
            String label = (isSelf ? "(You) " : "") + name + ": " + pops;
            int textColor = pops > 0 ? 0xFFFF5555 : 0xFF55FF55;
            ctx.drawTextWithShadow(mc.textRenderer, label, px, py, textColor);
            py += lineH;
        }
    }
}
