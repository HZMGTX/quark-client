package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;

public class AntiAFK extends Module {

    private final IntSetting interval = register(new IntSetting(
            "Interval", "Seconds between rotation", 30, 10, 120));

    private final BoolSetting swing = register(new BoolSetting(
            "Swing", "Also swing arm", false));

    private int tickCounter = 0;
    private float yawOffset = 5f;

    public AntiAFK() {
        super("AntiAFK", "Prevents AFK kick by periodically rotating", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        tickCounter++;
        if (tickCounter < interval.get() * 20) return;
        tickCounter = 0;

        mc.player.setYaw(mc.player.getYaw() + yawOffset);
        yawOffset = -yawOffset;

        if (swing.isEnabled() && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
    }
}
