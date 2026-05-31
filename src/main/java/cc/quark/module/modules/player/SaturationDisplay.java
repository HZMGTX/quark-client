package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.text.Text;

public class SaturationDisplay extends Module {

    private final TimerUtil timer = new TimerUtil();

    public SaturationDisplay() {
        super("SaturationDisplay", "Shows saturation as a numeric value every 5 seconds", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!timer.hasReached(5000)) return;
        timer.reset();

        float saturation = mc.player.getHungerManager().getSaturationLevel();
        String msg = String.format("Saturation: %.2f", saturation);
        mc.player.sendMessage(Text.literal(msg), true);
    }
}
