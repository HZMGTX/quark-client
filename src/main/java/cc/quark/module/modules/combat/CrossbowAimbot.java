package cc.quark.module.modules.combat;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * CrossbowAimbot - aims at the nearest player while holding a crossbow.
 */
public class CrossbowAimbot extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Target range", 30.0, 5.0, 64.0));

    public CrossbowAimbot() {
        super("CrossbowAimbot", "Aims a crossbow at the nearest player", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof CrossbowItem)) return;

        Entity best = null;
        double bestDist = range.get();
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof PlayerEntity player)) continue;
            if (Quark.getInstance().getFriendManager().isFriend(player.getGameProfile().getName())) continue;
            double d = mc.player.distanceTo(entity);
            if (d < bestDist) {
                bestDist = d;
                best = entity;
            }
        }

        if (best != null) {
            Vec3d eye = best.getEyePos();
            mc.player.setYaw(RotationUtil.getYaw(eye));
            mc.player.setPitch(MathHelper.clamp(RotationUtil.getPitch(eye), -90f, 90f));
        }
    }
}
