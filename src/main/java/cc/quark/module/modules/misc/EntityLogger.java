package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashSet;
import java.util.Set;

public class EntityLogger extends Module {

    private final BoolSetting players = register(new BoolSetting("Players", "Log player spawn/despawn", true));
    private final BoolSetting mobs    = register(new BoolSetting("Mobs",    "Log mob spawn/despawn",    false));
    private final BoolSetting items   = register(new BoolSetting("Items",   "Log item spawn/despawn",   false));

    private Set<Integer> tracked = new HashSet<>();

    public EntityLogger() {
        super("EntityLogger", "Logs entity spawn and despawn events to console", Category.MISC);
    }

    @Override
    public void onEnable() {
        tracked.clear();
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.world == null || mc.player == null) return;

        Set<Integer> current = new HashSet<>();
        for (Entity ent : mc.world.getEntities()) {
            if (ent == mc.player) continue;
            if (!shouldTrack(ent)) continue;
            current.add(ent.getId());
            if (!tracked.contains(ent.getId()))
                System.out.println("[EntityLogger] SPAWN " + ent.getType().getName().getString() + " at " + ent.getBlockPos());
        }
        for (int id : tracked) {
            if (!current.contains(id))
                System.out.println("[EntityLogger] DESPAWN id=" + id);
        }
        tracked = current;
    }

    private boolean shouldTrack(Entity e) {
        if (players.isEnabled() && e instanceof PlayerEntity) return true;
        if (mobs.isEnabled()    && e instanceof MobEntity)    return true;
        if (items.isEnabled()   && e instanceof ItemEntity)   return true;
        return false;
    }
}
