package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Collection;
import java.util.List;

public class PlayerCounter extends Module {

    private final IntSetting  posX       = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting  posY       = register(new IntSetting("Y", "HUD Y position", 14, 0, 3000));
    private final BoolSetting showList   = register(new BoolSetting("Show Tab Count", "Count from tab list (more accurate)", true));
    private final BoolSetting showNearby = register(new BoolSetting("Show Nearby", "Also show visible nearby player count", true));
    private final BoolSetting bgBox      = register(new BoolSetting("Background", "Draw background box", true));
    private final IntSetting  updateRate = register(new IntSetting("Update Rate", "Update every N ticks", 20, 1, 100));

    private int tabCount = 0;
    private int nearbyCount = 0;
    private int tick = 0;

    public PlayerCounter() {
        super("PlayerCounter", "Shows real-time count of players on the server", Category.MISC);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
        tick = 0;
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (++tick < updateRate.get()) return;
        tick = 0;

        // Tab list count
        if (showList.isEnabled() && mc.getNetworkHandler() != null) {
            Collection<PlayerListEntry> entries = mc.getNetworkHandler().getPlayerList();
            tabCount = entries != null ? entries.size() : 0;
        }

        // Nearby loaded players
        if (showNearby.isEnabled()) {
            List<? extends PlayerEntity> players = mc.world.getPlayers();
            nearbyCount = (int) players.stream()
                    .filter(p -> p != mc.player)
                    .count();
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int x = posX.get(), y = posY.get();
        int lh = mc.textRenderer.fontHeight + 1;

        String tabStr  = showList.isEnabled()   ? "Players: §f" + tabCount    : null;
        String nearStr = showNearby.isEnabled()  ? "Nearby: §f"  + nearbyCount : null;

        if (bgBox.isEnabled()) {
            int maxW = 0;
            if (tabStr  != null) maxW = Math.max(maxW, mc.textRenderer.getWidth(tabStr));
            if (nearStr != null) maxW = Math.max(maxW, mc.textRenderer.getWidth(nearStr));
            int lines = (tabStr != null ? 1 : 0) + (nearStr != null ? 1 : 0);
            ctx.fill(x - 2, y - 1, x + maxW + 2, y + lines * lh, 0x88000000);
        }

        if (tabStr != null) {
            ctx.drawTextWithShadow(mc.textRenderer, "§7" + tabStr, x, y, 0xFFFFFFFF);
            y += lh;
        }
        if (nearStr != null) {
            ctx.drawTextWithShadow(mc.textRenderer, "§7" + nearStr, x, y, 0xFFAAAAAA);
        }
    }
}
