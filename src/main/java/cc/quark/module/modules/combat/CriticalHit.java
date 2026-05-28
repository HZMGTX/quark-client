package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class CriticalHit extends Module {

    private final ModeSetting mode = register(new ModeSetting("Mode", "How to trigger critical hits", "Packet", "Packet", "Jump", "Legit"));

    private boolean pendingJump = false;
    private boolean attackedThisTick = false;

    public CriticalHit() {
        super("CriticalHit", "Makes every melee hit a critical hit", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        pendingJump = false;
        attackedThisTick = false;
    }

    @Override
    public void onDisable() {
        pendingJump = false;
        attackedThisTick = false;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (mc.player.isInLava() || mc.player.isTouchingWater() || mc.player.isClimbing()) return;

        String m = mode.get();
        if (m.equals("Packet")) {
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1.1E-5, z, false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true));
        } else if (m.equals("Jump")) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
                pendingJump = true;
                attackedThisTick = true;
            }
        } else if (m.equals("Legit")) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
            }
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (pendingJump && mc.player.isOnGround() && !attackedThisTick) {
            pendingJump = false;
        }

        attackedThisTick = false;
    }
}
