package cc.quark.module.modules.combat;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class AutoVelocity extends Module {

    private final BoolSetting onlyInCombat = register(new BoolSetting(
            "OnlyInCombat", "Only apply velocity management when an enemy is nearby", true));

    private final BoolSetting enableAntiVelocity = register(new BoolSetting(
            "Enable Anti-Velocity", "Activates AntiVelocity2 automatically when in combat", true));

    private final BoolSetting enableAntiKnockback = register(new BoolSetting(
            "Enable Anti-Knockback", "Activates AntiKnockback automatically when in combat", false));

    private boolean wasInCombat = false;

    public AutoVelocity() {
        super("AutoVelocity", "Manages velocity modules automatically based on combat context", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        setVelocityModules(false);
        wasInCombat = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean inCombat = false;
        if (onlyInCombat.isEnabled()) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity == mc.player) continue;
                if (!(entity instanceof PlayerEntity)) continue;
                if (mc.player.distanceTo(entity) <= 8.0) {
                    inCombat = true;
                    break;
                }
            }
        } else {
            inCombat = true;
        }

        if (inCombat != wasInCombat) {
            setVelocityModules(inCombat);
            wasInCombat = inCombat;
        }
    }

    private void setVelocityModules(boolean enable) {
        var mm = Quark.getInstance().getModuleManager();
        if (mm == null) return;

        if (enableAntiVelocity.isEnabled()) {
            var av = mm.getModule(AntiVelocity2.class);
            if (av != null && av.isEnabled() != enable) {
                if (enable) av.enable(); else av.disable();
            }
        }

        if (enableAntiKnockback.isEnabled()) {
            var ak = mm.getModule(AntiKnockback.class);
            if (ak != null && ak.isEnabled() != enable) {
                if (enable) ak.enable(); else ak.disable();
            }
        }
    }
}
