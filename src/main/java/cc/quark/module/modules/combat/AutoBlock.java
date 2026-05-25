package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;

public class AutoBlock extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Activate when enemy is within this many blocks", 5.0, 1.0, 10.0));
    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "Only Players", "Only shield against players", false));

    public AutoBlock() {
        super("AutoBlock", "Auto right-clicks with shield when an enemy is nearby", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.clearActiveItem();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!(mc.player.getOffHandStack().getItem() instanceof ShieldItem)) return;

        boolean enemyNear = false;
        double r = range.get();
        for (net.minecraft.entity.Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;
            if (onlyPlayers.isEnabled() && !(entity instanceof PlayerEntity)) continue;
            if (mc.player.distanceTo(entity) <= r) { enemyNear = true; break; }
        }

        if (enemyNear) {
            mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
        } else {
            mc.player.clearActiveItem();
        }
    }
}
