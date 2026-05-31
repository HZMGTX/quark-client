package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * PearlPredict - tracks in-flight ender pearls thrown by other players and
 * predicts where they will land, alerting the user via chat.
 *
 * <p>The prediction simulates vanilla pearl physics:
 * <ul>
 *   <li>Gravity: -0.03 blocks/tick² (pearl entity gravity)</li>
 *   <li>Drag: 0.99 horizontal, 0.98 vertical multiplier per tick</li>
 * </ul>
 */
public class PearlPredict extends Module {

    private final IntSetting simTicks = register(new IntSetting(
            "Sim Ticks", "How many ticks ahead to simulate pearl trajectory", 200, 20, 400));

    private final BoolSetting alertChat = register(new BoolSetting(
            "Alert Chat", "Print landing prediction in chat", true));

    private final BoolSetting trackOwn = register(new BoolSetting(
            "Track Own", "Also predict your own thrown pearls", false));

    // Pearl physics constants (Minecraft 1.21.1)
    private static final double GRAVITY = 0.03;
    private static final double DRAG_H  = 0.99;
    private static final double DRAG_V  = 0.98;

    private final List<EnderPearlEntity> trackedPearls = new ArrayList<>();

    public PearlPredict() {
        super("PearlPredict", "Predicts where thrown ender pearls will land", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        trackedPearls.clear();
    }

    @Override
    public void onDisable() {
        trackedPearls.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // Collect all active ender pearl entities
        List<EnderPearlEntity> currentPearls = new ArrayList<>();
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof EnderPearlEntity pearl)) continue;
            if (!trackOwn.isEnabled() && pearl.getOwner() == mc.player) continue;
            currentPearls.add(pearl);
        }

        // Alert on newly appeared pearls
        for (EnderPearlEntity pearl : currentPearls) {
            if (!trackedPearls.contains(pearl)) {
                trackedPearls.add(pearl);
                Vec3d landPos = simulatePearl(pearl);
                if (alertChat.isEnabled() && landPos != null) {
                    String owner = pearl.getOwner() != null ? pearl.getOwner().getName().getString() : "Unknown";
                    ChatUtil.info("[PearlPredict] " + owner + "'s pearl will land near "
                            + String.format("X:%.1f Y:%.1f Z:%.1f", landPos.x, landPos.y, landPos.z));
                }
            }
        }

        // Remove pearls that are no longer in the world
        trackedPearls.removeIf(p -> !currentPearls.contains(p));
    }

    /**
     * Simulates the pearl trajectory and returns the predicted landing position,
     * or {@code null} if it won't hit the ground within simTicks.
     */
    private Vec3d simulatePearl(EnderPearlEntity pearl) {
        if (mc.world == null) return null;

        Vec3d pos = pearl.getPos();
        Vec3d vel = pearl.getVelocity();
        int ticks = simTicks.get();

        for (int i = 0; i < ticks; i++) {
            // Apply drag
            vel = new Vec3d(vel.x * DRAG_H, vel.y * DRAG_V - GRAVITY, vel.z * DRAG_H);
            Vec3d nextPos = pos.add(vel);

            // Check if the next position is inside a solid block (simple ground check)
            BlockPos blockAt = BlockPos.ofFloored(nextPos);
            if (!mc.world.getBlockState(blockAt).isAir()) {
                return pos; // Return last airborne position as landing spot
            }

            // Check the block below — if we cross a block surface, that's the landing
            BlockPos blockBelow = BlockPos.ofFloored(pos.x, pos.y - 0.1, pos.z);
            if (!mc.world.getBlockState(blockBelow).isAir() && vel.y < 0) {
                return pos;
            }

            pos = nextPos;
        }

        return pos; // Return last predicted position if didn't land yet
    }
}
