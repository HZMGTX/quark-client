package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public class GlowESP extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Radius to apply glowing effect", 32, 8, 64));
    private final BoolSetting players = register(new BoolSetting("Players", "Apply glow to other players", true));
    private final BoolSetting mobs = register(new BoolSetting("Mobs", "Apply glow to hostile mobs", false));

    public GlowESP() {
        super("GlowESP", "Applies glowing effect to nearby players and mobs for outline ESP", Category.RENDER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        int r = range.get();
        double rangeSq = (double) r * r;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == mc.player) continue;
            if (mc.player.squaredDistanceTo(entity) > rangeSq) continue;

            boolean shouldGlow = false;
            if (players.isEnabled() && entity instanceof PlayerEntity) {
                shouldGlow = true;
            } else if (mobs.isEnabled() && entity instanceof MobEntity) {
                shouldGlow = true;
            }

            if (shouldGlow) {
                living.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.GLOWING, 60, 0, false, false));
            }
        }
    }
}
