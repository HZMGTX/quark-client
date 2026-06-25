package cc.quark.module.modules.combat;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * CrossbowAimbot — silently aims at the nearest enemy player while holding a
 * crossbow.  Rotation is applied via EventPreMotion (server-side yaw/pitch)
 * so the client model does not visibly snap.
 * Auto-fire releases the crossbow when fully loaded.
 */
public class CrossbowAimbot extends Module {

    private final DoubleSetting range    = register(new DoubleSetting("Range",     "Target range",                     30.0,  5.0, 64.0));
    private final BoolSetting   autoFire = register(new BoolSetting  ("Auto Fire", "Auto-fire when crossbow is ready",  true));
    private final BoolSetting   autoLoad = register(new BoolSetting  ("Auto Load", "Automatically load the crossbow",   true));

    private float targetYaw   = 0f;
    private float targetPitch = 0f;
    private boolean hasTarget = false;

    public CrossbowAimbot() {
        super("CrossbowAimbot", "Silent aim at nearest player with crossbow; auto-load and fire", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        hasTarget = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        ItemStack held = mc.player.getMainHandStack();
        if (!(held.getItem() instanceof CrossbowItem)) { hasTarget = false; return; }

        // Auto-load: start charging if not loaded and not already using
        if (autoLoad.isEnabled() && !CrossbowItem.isCharged(held) && !mc.player.isUsingItem()) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            return;
        }

        // Auto-fire: release when charged
        if (autoFire.isEnabled() && CrossbowItem.isCharged(held) && hasTarget) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
            return;
        }

        // Find target
        PlayerEntity target = findTarget();
        if (target == null) { hasTarget = false; return; }
        hasTarget = true;

        Vec3d eye = target.getEyePos();
        targetYaw   = RotationUtil.getYaw(eye);
        targetPitch = MathHelper.clamp(RotationUtil.getPitch(eye), -90f, 90f);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (!hasTarget) return;
        event.setYaw(targetYaw);
        event.setPitch(targetPitch);
    }

    private PlayerEntity findTarget() {
        PlayerEntity best = null;
        double bestDist = range.get();
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity p)) continue;
            if (p == mc.player) continue;
            if (p.isRemoved() || p.getHealth() <= 0f) continue;
            if (Quark.getInstance() != null
                    && Quark.getInstance().getFriendManager().isFriend(p.getGameProfile().getName())) continue;
            double d = mc.player.distanceTo(p);
            if (d < bestDist) { bestDist = d; best = p; }
        }
        return best;
    }
}
