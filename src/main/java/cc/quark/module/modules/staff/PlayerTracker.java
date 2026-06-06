package cc.quark.module.modules.staff;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class PlayerTracker extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Range", 64, 16, 256));
    private final BoolSetting showCoords = register(new BoolSetting("ShowCoords", "ShowCoords", true));
    private final BoolSetting showHealth = register(new BoolSetting("ShowHealth", "ShowHealth", true));
    private final IntSetting maxDisplay  = register(new IntSetting("MaxDisplay", "MaxDisplay", 10, 5, 30));

    private final Map<UUID, Vec3d> lastPositions = new HashMap<>();
    private final Map<UUID, Float> lastHealths   = new HashMap<>();

    public PlayerTracker() {
        super("PlayerTracker", "Tracks all nearby players' positions and health in a HUD list", Category.STAFF);
    }


    @EventHandler
    public void onTick(EventTick event) {
        
        if (mc == null || mc.player == null || mc.world == null) return;
        mc.world.getPlayers().forEach(p -> {
            if (p == mc.player) return;
            lastPositions.put(p.getUuid(), p.getPos());
            lastHealths.put(p.getUuid(), p.getHealth());
        });
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        
        if (mc == null || mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();
        int x = 4, y = 40, count = 0;
        ctx.drawTextWithShadow(mc.textRenderer, "§c[Tracker]", x, y, 0xFFFF5555);
        y += 10;

        Vec3d myPos = mc.player.getPos();
        List<PlayerEntity> players = new ArrayList<>(mc.world.getPlayers());
        players.sort(Comparator.comparingDouble(p -> p.getPos().distanceTo(myPos)));

        for (PlayerEntity p : players) {
            if (p == mc.player) continue;
            if (count >= maxDisplay.get()) break;
            if (p.getPos().distanceTo(myPos) > range.get()) continue;

            String name = p.getGameProfile().getName();
            String info = "§f" + name;
            if (showHealth.isEnabled()) info += " §c" + String.format("%.1f", p.getHealth()) + "hp";
            if (showCoords.isEnabled()) {
                Vec3d pos = p.getPos();
                info += " §7(" + (int)pos.x + "," + (int)pos.y + "," + (int)pos.z + ")";
            }
            ctx.drawTextWithShadow(mc.textRenderer, info, x, y, 0xFFFFFFFF);
            y += 10; count++;
        }
    }
}
