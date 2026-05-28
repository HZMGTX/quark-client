package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;

public class Stab extends Module {

    private final BoolSetting onlySprint = register(new BoolSetting("Only Sprint", "Only stab when sprinting", true));
    private final IntSetting stabDelay = register(new IntSetting("Stab Delay ms", "Milliseconds to hold W released after attack", 100, 50, 500));

    private final TimerUtil attackTimer = new TimerUtil();
    private int releaseTicks = 0;
    private boolean wWasPressed = false;

    public Stab() {
        super("Stab", "Sprint-resets between hits by briefly releasing W key", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        releaseTicks = 0;
        wWasPressed = false;
        attackTimer.reset();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (releaseTicks > 0) {
            mc.options.forwardKey.setPressed(false);
            releaseTicks = 0;
        }
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        if (onlySprint.isEnabled() && !mc.player.isSprinting()) return;
        if (mc.player.input.movementForward <= 0) return;
        if (!attackTimer.hasReached(stabDelay.get())) return;

        wWasPressed = true;
        mc.player.setSprinting(false);
        mc.options.forwardKey.setPressed(false);
        releaseTicks = Math.max(1, stabDelay.get() / 50);
        attackTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (releaseTicks > 0) {
            releaseTicks--;
            if (releaseTicks == 0) {
                if (wWasPressed) {
                    mc.options.forwardKey.setPressed(true);
                }
                mc.player.setSprinting(true);
                wWasPressed = false;
            }
        }
    }
}
