package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.TimerUtil;
import net.minecraft.text.Text;

public class ComboExtender extends Module {

    private int combo = 0;
    private final TimerUtil comboTimer = new TimerUtil();
    private static final long COMBO_RESET_MS = 3000;

    public ComboExtender() {
        super("ComboExtender", "Tracks combo hit count and displays current streak", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        combo = 0;
    }

    @Override
    public void onDisable() {
        combo = 0;
    }

    @Override
    public String getSuffix() {
        return "x" + combo;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        if (comboTimer.hasReached(COMBO_RESET_MS)) {
            combo = 0;
        }
        combo++;
        comboTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (comboTimer.hasReached(COMBO_RESET_MS) && combo > 0) {
            combo = 0;
        }
        if (combo > 0) {
            mc.player.sendMessage(Text.literal("Combo: x" + combo), true);
        }
    }
}
