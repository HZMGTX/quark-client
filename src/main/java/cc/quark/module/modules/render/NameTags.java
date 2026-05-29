package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * NameTags - enhanced name tags with health / distance / ping info,
 * rendered in 2D screen-space via RenderUtil.project.
 */
public class NameTags extends Module {

    private final BoolSetting players     = register(new BoolSetting("Players",     "Show tags on players",           true));
    private final BoolSetting mobs        = register(new BoolSetting("Mobs",        "Show tags on mobs",              false));
    private final BoolSetting showHealth  = register(new BoolSetting("Show Health", "Include health in tag",          true));
    private final BoolSetting showDistance= register(new BoolSetting("Show Dist",   "Include distance in tag",        true));
    private final BoolSetting showPing    = register(new BoolSetting("Show Ping",   "Include ping (players only)",    true));

    public NameTags() {
        super("NameTags", "Enhanced name tags above entities with health/distance/ping info", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;

        DrawContext ctx = event.getDrawContext();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead()) continue;

            boolean isPlayer = entity instanceof PlayerEntity;
            boolean isMob    = entity instanceof MobEntity;

            if (isPlayer && !players.isEnabled()) continue;
            if (isMob    && !mobs.isEnabled())    continue;
            if (!isPlayer && !isMob)              continue;

            Vec3d headPos = new Vec3d(entity.getX(), entity.getY() + entity.getHeight() + 0.3, entity.getZ());
            double[] screen = RenderUtil.project(headPos);
            if (screen == null) continue;

            int sx = (int) screen[0];
            int sy = (int) screen[1];

            StringBuilder tag = new StringBuilder(entity.getDisplayName().getString());

            if (showHealth.isEnabled()) {
                float pct = living.getMaxHealth() > 0 ? living.getHealth() / living.getMaxHealth() : 1f;
                int hearts = (int) Math.ceil(living.getHealth() / 2f);
                tag.append(" ").append(String.format("%.1f HP", living.getHealth()));
            }

            if (showDistance.isEnabled()) {
                double dist = mc.player.distanceTo(entity);
                tag.append(" ").append(String.format("%.0fm", dist));
            }

            if (showPing.isEnabled() && isPlayer) {
                PlayerListEntry entry = mc.getNetworkHandler() != null
                        ? mc.getNetworkHandler().getPlayerListEntry(((PlayerEntity) entity).getGameProfile().getId())
                        : null;
                if (entry != null) {
                    tag.append(" ").append(entry.getLatency()).append("ms");
                }
            }

            int textW = mc.textRenderer.getWidth(tag.toString());
            RenderUtil.drawCustomText(ctx, tag.toString(), sx - textW / 2, sy, 0xFFFFFFFF);
        }
    }
}
