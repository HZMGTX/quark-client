package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;

public class HitboxExpand extends Module {

    private final DoubleSetting expansion = register(new DoubleSetting("Expansion", "Entity hitbox expansion amount", 0.1, 0.05, 0.5));

    public HitboxExpand() {
        super("HitboxExpand", "Slightly expands entity hitboxes client-side for easier hits", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        double exp = expansion.get();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity)) continue;
            Box original = entity.getBoundingBox();
            Box expanded = original.expand(exp);
            entity.setBoundingBox(expanded);
        }
    }
}
