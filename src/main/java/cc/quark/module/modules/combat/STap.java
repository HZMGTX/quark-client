package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;

public class STap extends Module {

    private final IntSetting durationMs = register(new IntSetting(
            "Duration Ms", "Milliseconds to hold backward key", 40, 10, 200));

    private final BoolSetting onlyOnAttack = register(new BoolSetting(
            "Only On Attack", "Only perform S-tap when actively attacking", true));

    private final TimerUtil tapTimer = new TimerUtil();
    private boolean tapping = false;
    private long lastAttackMs = 0L;

    public STap() {
        super("STap", "Performs S-tap (briefly presses backward) to reset sprint", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.backKey.setPressed(false);
        }
        tapping = false;
    }

    public void notifyAttack() {
        lastAttackMs = System.currentTimeMillis();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (tapping) {
            if (tapTimer.hasReached(durationMs.get())) {
                mc.options.backKey.setPressed(false);
                tapping = false;
            }
            return;
        }

        if (onlyOnAttack.isEnabled()) {
            // Only S-tap shortly after an attack
            if (System.currentTimeMillis() - lastAttackMs > 200) return;
        }

        // Only S-tap when sprinting and moving forward
        if (!mc.player.isSprinting()) return;
        if (mc.player.input.movementForward <= 0) return;

        // Perform S-tap: briefly press backward to break sprint
        mc.player.setSprinting(false);
        mc.options.backKey.setPressed(true);
        tapping = true;
        tapTimer.reset();
    }
}
