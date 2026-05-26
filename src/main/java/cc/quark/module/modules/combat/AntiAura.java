package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * AntiAura - jumps away when a player gets suspiciously close, dodging killaura.
 */
public class AntiAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Trigger range", 3.0, 1.0, 6.0));

    public AntiAura() {
        super("AntiAura", "Jumps to dodge enemy killaura", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof PlayerEntity player) || player.isDead()) continue;
            if (mc.player.distanceTo(entity) <= range.get() && mc.player.isOnGround()) {
                mc.player.input.jumping = true;
                return;
            }
        }
    }
}
