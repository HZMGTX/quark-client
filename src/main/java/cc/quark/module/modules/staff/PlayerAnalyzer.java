package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

import java.util.HashMap;
import java.util.Map;

public class PlayerAnalyzer extends Module {

    private final BoolSetting detectSpeed = register(new BoolSetting(
            "Detect Speed", "Flag players moving abnormally fast", true));
    private final BoolSetting detectFly = register(new BoolSetting(
            "Detect Fly", "Flag players who appear to be flying without effects", true));
    private final BoolSetting detectNoFall = register(new BoolSetting(
            "Detect NoFall", "Flag players who take no fall damage", true));
    private final DoubleSetting speedThreshold = register(new DoubleSetting(
            "Speed Threshold", "Horizontal speed (blocks/tick) to flag as suspicious", 0.7, 0.3, 5.0));
    private final IntSetting flagThreshold = register(new IntSetting(
            "Flag Threshold", "Number of flags before sending a chat alert", 3, 1, 20));
    private final BoolSetting logToChat = register(new BoolSetting(
            "Log To Chat", "Print flagged events to local chat", true));
    private final StringSetting watchedPlayer = register(new StringSetting(
            "Watched Player", "Focus analysis on one player (empty = all)", ""));

    // name -> flag count
    private final Map<String, Integer> flags = new HashMap<>();
    // name -> last Y position for fly/fall detection
    private final Map<String, Double> lastY = new HashMap<>();
    // name -> last on-ground tick
    private final Map<String, Integer> airTicks = new HashMap<>();
    private int currentTick = 0;

    public PlayerAnalyzer() {
        super("PlayerAnalyzer", "Analyzes player packets and flags suspicious activity", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        mc.getEventBus().subscribe(this);
        flags.clear();
        lastY.clear();
        airTicks.clear();
        ChatUtil.info("§6[PlayerAnalyzer] §fAnalysis started.");
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        flags.clear();
        lastY.clear();
        airTicks.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;
        currentTick++;

        String filter = watchedPlayer.get().trim().toLowerCase();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            if (player == mc.player) continue;

            String name = player.getGameProfile().getName();
            if (!filter.isEmpty() && !name.toLowerCase().contains(filter)) continue;

            // Speed check
            if (detectSpeed.isEnabled()) {
                double dx = player.getX() - player.prevX;
                double dz = player.getZ() - player.prevZ;
                double horizSpeed = Math.sqrt(dx * dx + dz * dz);
                if (horizSpeed > speedThreshold.get()) {
                    addFlag(name, String.format("Speed %.2f b/t", horizSpeed));
                }
            }

            // Fly check: player in air for many ticks without falling
            if (detectFly.isEnabled()) {
                if (!player.isOnGround() && !player.isTouchingWater() && !player.isInLava()) {
                    int air = airTicks.getOrDefault(name, 0) + 1;
                    airTicks.put(name, air);
                    Double prevY = lastY.get(name);
                    if (prevY != null && air > 20) {
                        double yDelta = player.getY() - prevY;
                        // Sustained upward movement or hovering = suspicious
                        if (yDelta >= -0.01 && yDelta <= 0.01) {
                            addFlag(name, "Hovering (air ticks: " + air + ")");
                        } else if (yDelta > 0.1) {
                            addFlag(name, "Ascending in air (air ticks: " + air + ")");
                        }
                    }
                } else {
                    airTicks.put(name, 0);
                }
            }

            // NoFall check: player falls from height but Y resets without on-ground bounce
            if (detectNoFall.isEnabled()) {
                Double prevY = lastY.get(name);
                if (prevY != null) {
                    double fall = prevY - player.getY();
                    if (fall > 3.0 && player.isOnGround() && !player.isDead()) {
                        // They landed from >3 block fall; check fallDistance
                        if (player.fallDistance < 0.01f) {
                            addFlag(name, String.format("NoFall (drop=%.1f, fallDist=%.2f)", fall, player.fallDistance));
                        }
                    }
                }
            }

            lastY.put(name, player.getY());
        }
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        // Watch for suspicious velocity zeroing (may indicate KillAura or anti-knockback)
        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket pkt) {
            if (mc.world == null) return;
            Entity e = mc.world.getEntityById(pkt.getId());
            if (!(e instanceof PlayerEntity player)) return;
            if (player == mc.player) return;
            // Velocity of exactly (0,0,0) set by server can indicate anti-kb hooks on the entity
            if (pkt.getVelocityX() == 0 && pkt.getVelocityY() == 0 && pkt.getVelocityZ() == 0) {
                String name = player.getGameProfile().getName();
                String filter = watchedPlayer.get().trim().toLowerCase();
                if (filter.isEmpty() || name.toLowerCase().contains(filter)) {
                    addFlag(name, "Zero-velocity packet (possible anti-KB)");
                }
            }
        }
    }

    private void addFlag(String playerName, String reason) {
        int count = flags.getOrDefault(playerName, 0) + 1;
        flags.put(playerName, count);
        if (logToChat.isEnabled() && count % flagThreshold.get() == 0) {
            ChatUtil.warn("§c[PlayerAnalyzer] §f" + playerName
                    + " §cflagged §7(x" + count + ")§f: " + reason);
        }
    }
}
