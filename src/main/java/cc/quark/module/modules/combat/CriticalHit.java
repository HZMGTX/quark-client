package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;

public class CriticalHit extends Module {

    private final ModeSetting mode = register(new ModeSetting("Mode", "How to trigger critical hits", "Packet", "Packet", "Jump"));
    private boolean shouldJump = false;

    public CriticalHit() {
        super("CriticalHit", "Makes every hit a critical hit", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        if (mode.is("Packet")) {
            if (!mc.player.isOnGround()) return;
            if (mc.getNetworkHandler() == null) return;
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1.1E-5, z, false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true));
        } else {
            shouldJump = true;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (shouldJump && mc.player.isOnGround()) {
            mc.player.jump();
            shouldJump = false;
        }
    }
}
