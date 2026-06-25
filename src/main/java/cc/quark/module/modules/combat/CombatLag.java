package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * CombatLag - periodically injects a burst of no-op position packets during
 * combat to simulate a brief lag spike, disrupting anti-cheat timing analysis
 * and making attack patterns harder to detect.
 *
 * <p>This works by sending duplicate position packets that carry the player's
 * current position, effectively inflating packet timestamps and confusing
 * timing-based detections.
 */
public class CombatLag extends Module {

    private final IntSetting lagIntervalMs = register(new IntSetting(
            "Interval", "How often to trigger a lag burst (ms)", 3000, 500, 10000));

    private final IntSetting burstPackets = register(new IntSetting(
            "Burst Size", "Number of extra packets per lag burst", 5, 1, 20));

    private final IntSetting combatRange = register(new IntSetting(
            "Range", "Only trigger when enemy is within this range (blocks)", 8, 2, 20));

    private final BoolSetting onlyInCombat = register(new BoolSetting(
            "Only In Combat", "Only send lag packets when an enemy is nearby", true));

    private final TimerUtil lagTimer = new TimerUtil();

    public CombatLag() {
        super("CombatLag", "Sends burst packets during combat to disrupt timing analysis", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lagTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!lagTimer.hasReached(lagIntervalMs.get())) return;

        if (onlyInCombat.isEnabled() && !isEnemyNearby()) return;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        boolean onGround = mc.player.isOnGround();

        // Send burst of duplicate position packets
        int count = burstPackets.get();
        for (int i = 0; i < count; i++) {
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, onGround));
        }

        lagTimer.reset();
    }

    private boolean isEnemyNearby() {
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity p)) continue;
            if (p.isDead() || p.getHealth() <= 0f) continue;
            if (mc.player.distanceTo(p) <= combatRange.get()) return true;
        }
        return false;
    }
}
