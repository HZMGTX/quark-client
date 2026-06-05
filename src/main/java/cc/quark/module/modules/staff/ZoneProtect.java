package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.HashMap;
import java.util.Map;

public class ZoneProtect extends Module {

    private final StringSetting cornerA = register(new StringSetting(
            "Corner A", "First corner of zone as x,y,z (blank = set from position)", ""));
    private final StringSetting cornerB = register(new StringSetting(
            "Corner B", "Second corner of zone as x,y,z (blank = set from position)", ""));
    private final BoolSetting warnPlayer = register(new BoolSetting(
            "Warn Player", "Whisper warning to player who enters the zone", true));
    private final BoolSetting kickOnEnter = register(new BoolSetting(
            "Kick On Enter", "Kick player when they enter the zone", false));
    private final StringSetting warnMessage = register(new StringSetting(
            "Warn Message", "Warning sent to player (use {name} as placeholder)", "Warning: You have entered a restricted zone!"));
    private final IntSetting cooldownTicks = register(new IntSetting(
            "Cooldown Ticks", "Ticks between repeated warnings for the same player", 100, 20, 1200));
    private final BoolSetting markFromPos = register(new BoolSetting(
            "Mark From Pos", "On enable: auto-set Corner A to your current position", false));

    // name -> last tick warned
    private final Map<String, Integer> lastWarned = new HashMap<>();
    private int currentTick = 0;

    // Parsed zone box; null if not configured
    private Box zone = null;

    public ZoneProtect() {
        super("ZoneProtect", "Marks a zone and warns players who enter it", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        mc.getEventBus().subscribe(this);
        lastWarned.clear();
        currentTick = 0;
        zone = null;

        // Auto-mark Corner A from current position if setting enabled
        if (markFromPos.isEnabled()) {
            BlockPos pos = mc.player.getBlockPos();
            cornerA.setValue(pos.getX() + "," + pos.getY() + "," + pos.getZ());
            ChatUtil.info("§6[ZoneProtect] §fCorner A set to §e" + cornerA.get());
        }

        zone = buildZone();
        if (zone != null) {
            ChatUtil.info("§6[ZoneProtect] §fZone active: §e"
                    + cornerA.get() + " §f-> §e" + cornerB.get());
        } else {
            ChatUtil.warn("§6[ZoneProtect] §eZone not configured. Set Corner A and Corner B.");
        }
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        lastWarned.clear();
        zone = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;
        currentTick++;

        // Rebuild zone in case settings changed
        zone = buildZone();
        if (zone == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            if (player == mc.player) continue;

            String name = player.getGameProfile().getName();

            // Check if the player is inside the zone
            if (!zone.intersects(player.getBoundingBox())) continue;

            // Apply cooldown
            int lastWarn = lastWarned.getOrDefault(name, -cooldownTicks.get());
            if (currentTick - lastWarn < cooldownTicks.get()) continue;
            lastWarned.put(name, currentTick);

            // Warn the player
            if (warnPlayer.isEnabled()) {
                String msg = warnMessage.get().replace("{name}", name);
                mc.player.networkHandler.sendChatCommand("msg " + name + " " + msg);
                ChatUtil.info("§6[ZoneProtect] §fWarned §e" + name + " §f: " + msg);
            }

            // Kick the player if configured
            if (kickOnEnter.isEnabled()) {
                mc.player.networkHandler.sendChatCommand("kick " + name + " Restricted zone violation");
                ChatUtil.warn("§6[ZoneProtect] §fKicked §e" + name + " §ffor zone entry.");
            }
        }
    }

    /**
     * Parses cornerA and cornerB settings into a Box. Returns null if either is invalid.
     */
    private Box buildZone() {
        double[] a = parseCoord(cornerA.get());
        double[] b = parseCoord(cornerB.get());
        if (a == null || b == null) return null;
        return new Box(
                Math.min(a[0], b[0]), Math.min(a[1], b[1]), Math.min(a[2], b[2]),
                Math.max(a[0], b[0]) + 1, Math.max(a[1], b[1]) + 1, Math.max(a[2], b[2]) + 1
        );
    }

    private double[] parseCoord(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        String[] parts = s.trim().split(",");
        if (parts.length < 3) return null;
        try {
            return new double[]{
                    Double.parseDouble(parts[0].trim()),
                    Double.parseDouble(parts[1].trim()),
                    Double.parseDouble(parts[2].trim())
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
