package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class SmartCrit extends Module {

    private final ModeSetting mode = register(new ModeSetting("Mode", "Critical hit mode", "Packet", "Packet", "Jump", "Legit"));

    private boolean doPacketCrit = false;
    private int legitPhase = 0;

    public SmartCrit() {
        super("SmartCrit", "Packet-based critical hits on attack", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        doPacketCrit = false;
        legitPhase = 0;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        if (mc.player.isTouchingWater() || mc.player.isInLava()) return;
        if (mc.player.getAttackCooldownProgress(0f) < 0.9f) return;

        if (mode.is("Packet")) {
            if (mc.player.isOnGround()) {
                doPacketCrit = true;
            }
        } else if (mode.is("Jump")) {
            if (mc.player.isOnGround()) {
                mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
                mc.player.setOnGround(false);
            }
        } else if (mode.is("Legit")) {
            if (mc.player.isOnGround()) {
                mc.player.setVelocity(mc.player.getVelocity().x, 0.18, mc.player.getVelocity().z);
                legitPhase = 1;
            }
        }
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        if (mode.is("Packet") && doPacketCrit) {
            doPacketCrit = false;
            double x = event.getX();
            double y = event.getY();
            double z = event.getZ();
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.11, z, false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.1100013579, z, false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0000013579, z, false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
            event.setOnGround(false);
        }

        if (mode.is("Legit") && legitPhase > 0) {
            event.setOnGround(false);
            legitPhase++;
            if (legitPhase > 3) legitPhase = 0;
        }
    }
}
