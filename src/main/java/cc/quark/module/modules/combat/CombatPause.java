package cc.quark.module.modules.combat;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashSet;
import java.util.Set;

public class CombatPause extends Module {

    private final BoolSetting autoResume = register(new BoolSetting(
            "AutoResume", "Re-enable paused modules when combat ends", true));

    private static final String[] AUTOMATION_MODULES = {
            "AutoBuild", "AutoMine", "AutoFarm", "ElytraFly", "Scaffold"
    };

    private final Set<String> pausedModules = new HashSet<>();
    private boolean wasInCombat = false;

    public CombatPause() {
        super("CombatPause", "Pauses all automation modules during active PvP", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        if (autoResume.isEnabled()) {
            resumeModules();
        }
        pausedModules.clear();
        wasInCombat = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean inCombat = nearEnemy();

        if (inCombat && !wasInCombat) {
            pauseModules();
            wasInCombat = true;
        } else if (!inCombat && wasInCombat) {
            if (autoResume.isEnabled()) {
                resumeModules();
            }
            wasInCombat = false;
        }
    }

    private boolean nearEnemy() {
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity)) continue;
            if (mc.player.distanceTo(e) <= 10.0) return true;
        }
        return false;
    }

    private void pauseModules() {
        var mm = Quark.getInstance().getModuleManager();
        if (mm == null) return;
        pausedModules.clear();
        for (String name : AUTOMATION_MODULES) {
            var mod = mm.getModule(name);
            if (mod != null && mod.isEnabled()) {
                mod.disable();
                pausedModules.add(name);
            }
        }
    }

    private void resumeModules() {
        var mm = Quark.getInstance().getModuleManager();
        if (mm == null) return;
        for (String name : pausedModules) {
            var mod = mm.getModule(name);
            if (mod != null && !mod.isEnabled()) {
                mod.enable();
            }
        }
        pausedModules.clear();
    }
}
