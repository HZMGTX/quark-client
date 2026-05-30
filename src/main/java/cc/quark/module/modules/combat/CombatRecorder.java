package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * CombatRecorder — records the player's position whenever an enemy player is
 * nearby. Prints a summary when the module is disabled.
 */
public class CombatRecorder extends Module {

    private final BoolSetting autoRecord   = register(new BoolSetting("AutoRecord",   "Automatically record when enemy is nearby", true));
    private final IntSetting  maxPositions = register(new IntSetting ("MaxPositions", "Maximum stored positions",                  100, 20, 500));

    private final List<Vec3d> positions = new ArrayList<>();

    public CombatRecorder() {
        super("CombatRecorder", "Records player positions during combat and prints a summary on disable", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        positions.clear();
    }

    @Override
    public void onDisable() {
        if (positions.isEmpty()) {
            ChatUtil.info("CombatRecorder: No positions recorded.");
            return;
        }
        Vec3d start = positions.get(0);
        Vec3d end   = positions.get(positions.size() - 1);
        ChatUtil.info(String.format("CombatRecorder: %d positions recorded.", positions.size()));
        ChatUtil.info(String.format("Start: %.1f, %.1f, %.1f", start.x, start.y, start.z));
        ChatUtil.info(String.format("End:   %.1f, %.1f, %.1f", end.x,   end.y,   end.z));
        positions.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!autoRecord.isEnabled()) return;

        // Check if an enemy player is within 16 blocks
        boolean inCombat = false;
        for (PlayerEntity other : mc.world.getPlayers()) {
            if (other == mc.player) continue;
            if (mc.player.distanceTo(other) <= 16.0) {
                inCombat = true;
                break;
            }
        }

        if (!inCombat) return;

        if (positions.size() >= maxPositions.get()) {
            positions.remove(0);
        }
        positions.add(mc.player.getPos());
    }
}
