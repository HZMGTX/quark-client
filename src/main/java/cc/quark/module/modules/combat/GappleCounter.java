package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;

import java.util.HashMap;
import java.util.Map;

public class GappleCounter extends Module {
    private final IntSetting x = register(new IntSetting("X", "HUD X", 2, 0, 1920));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y", 50, 0, 1080));
    private final BoolSetting alertOnUse = register(new BoolSetting("Alert On Use", "Chat alert when enemy gaps", true));

    private final Map<String, Boolean> wasEating = new HashMap<>();

    public GappleCounter() {
        super("Gapple Counter", "Track when enemies eat golden apples", Category.COMBAT, 0);
    }

    @Override
    public void onDisable() {
        wasEating.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        for (var entity : mc.world.getPlayers()) {
            if (entity == mc.player) continue;
            String name = entity.getName().getString();
            boolean isEatingGap = entity.isUsingItem() &&
                (entity.getActiveItem().isOf(Items.GOLDEN_APPLE) || entity.getActiveItem().isOf(Items.ENCHANTED_GOLDEN_APPLE));
            Boolean was = wasEating.getOrDefault(name, false);
            if (isEatingGap && !was && alertOnUse.isEnabled()) {
                ChatUtil.warn("[Gap] §c" + name + " §fis eating a §6golden apple§f!");
            }
            wasEating.put(name, isEatingGap);
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();
        int yOff = 0;
        for (var entity : mc.world.getPlayers()) {
            if (entity == mc.player) continue;
            if (entity.isUsingItem() && (entity.getActiveItem().isOf(Items.GOLDEN_APPLE) || entity.getActiveItem().isOf(Items.ENCHANTED_GOLDEN_APPLE))) {
                ctx.drawText(mc.textRenderer, "§6[GAP] §f" + entity.getName().getString(), x.get(), y.get() + yOff, 0xFFFFFF, true);
                yOff += 10;
            }
        }
    }
}
