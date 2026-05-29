package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;

public class AntiAFK2 extends Module {

    private final IntSetting interval = register(new IntSetting(
            "Interval", "Seconds between AFK prevention actions", 25, 5, 120));
    private final BoolSetting swingArm = register(new BoolSetting(
            "Swing Arm", "Swing main hand periodically", true));
    private final BoolSetting sneakToggle = register(new BoolSetting(
            "Sneak Toggle", "Briefly sneak then unsneak", true));

    private final TimerUtil timer = new TimerUtil();
    private boolean isSneaking = false;
    private int sneakTicks = 0;

    public AntiAFK2() {
        super("AntiAFK2", "Alternate AFK prevention: arm swing + sneak/unsneak", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
        isSneaking = false;
        sneakTicks = 0;
    }

    @Override
    public void onDisable() {
        if (isSneaking && mc.player != null && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(
                    mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            isSneaking = false;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        // Release sneak after 2 ticks
        if (isSneaking) {
            if (++sneakTicks >= 2) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(
                        mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                isSneaking = false;
                sneakTicks = 0;
            }
            return;
        }

        if (!timer.hasReached(interval.get() * 1000L)) return;
        timer.reset();

        if (swingArm.isEnabled()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        if (sneakToggle.isEnabled()) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(
                    mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            isSneaking = true;
            sneakTicks = 0;
        }
    }
}
