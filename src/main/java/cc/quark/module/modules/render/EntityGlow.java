package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class EntityGlow extends Module {
    private final BoolSetting playersOnly = register(new BoolSetting("PlayersOnly", "Only glow players", true));
    private final BoolSetting mobs = register(new BoolSetting("Mobs", "Glow mobs too", false));
    public EntityGlow() { super("EntityGlow", "Makes entities glow through walls", Category.RENDER); }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null) return;
        mc.world.getEntities().forEach(e -> {
            if (e == mc.player) return;
            if (e instanceof PlayerEntity && playersOnly.getValue()) { e.setGlowing(true); return; }
            if (e instanceof LivingEntity && mobs.getValue()) e.setGlowing(true);
        });
    }
    @Override
    public void onDisable() {
        if (mc.world == null) return;
        mc.world.getEntities().forEach(e -> e.setGlowing(false));
    }
}
