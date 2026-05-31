package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * DebugHUD — shows a configurable debug info overlay including FPS, ping,
 * coordinates, velocity, dimension, and biome — a clean replacement for F3.
 */
public class DebugHUD extends Module {

    private final IntSetting xPos = register(new IntSetting(
            "X", "Horizontal position", 5, 0, 3000));

    private final IntSetting yPos = register(new IntSetting(
            "Y", "Vertical position", 5, 0, 3000));

    private final BoolSetting showFps = register(new BoolSetting(
            "FPS", "Show frames per second", true));

    private final BoolSetting showPing = register(new BoolSetting(
            "Ping", "Show server ping in ms", true));

    private final BoolSetting showCoords = register(new BoolSetting(
            "Coords", "Show player coordinates", true));

    private final BoolSetting showVelocity = register(new BoolSetting(
            "Velocity", "Show horizontal velocity (m/s)", true));

    private final BoolSetting showDimension = register(new BoolSetting(
            "Dimension", "Show current dimension", true));

    private final BoolSetting showBiome = register(new BoolSetting(
            "Biome", "Show current biome", true));

    private final BoolSetting showLightLevel = register(new BoolSetting(
            "Light Level", "Show block light level at feet", true));

    private final BoolSetting showBackground = register(new BoolSetting(
            "Background", "Draw dark background behind text", true));

    // BPS rolling average
    private final Deque<Double> bpsHistory = new ArrayDeque<>();
    private double prevX, prevZ;

    public DebugHUD() {
        super("DebugHUD", "Shows debug info overlay (FPS, ping, coords)", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            prevX = mc.player.getX();
            prevZ = mc.player.getZ();
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        ClientPlayerEntity player = mc.player;

        // Update rolling BPS
        double dx = player.getX() - prevX;
        double dz = player.getZ() - prevZ;
        double speed = Math.sqrt(dx * dx + dz * dz) * 20.0;
        prevX = player.getX();
        prevZ = player.getZ();
        bpsHistory.addLast(speed);
        while (bpsHistory.size() > 5) bpsHistory.pollFirst();
        double bps = bpsHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        DrawContext ctx = event.getDrawContext();
        int x = xPos.get();
        int y = yPos.get();
        int lineH = mc.textRenderer.fontHeight + 2;
        int padX = 4, padY = 3;

        // Collect lines
        java.util.List<String> lines = new java.util.ArrayList<>();

        if (showFps.isEnabled()) {
            int fps = mc.getCurrentFps();
            String fpsColor = fps >= 60 ? "§a" : fps >= 30 ? "§e" : "§c";
            lines.add(fpsColor + "FPS: §f" + fps);
        }
        if (showPing.isEnabled()) {
            int ping = getPing();
            String pingColor = ping < 80 ? "§a" : ping < 150 ? "§e" : "§c";
            lines.add(pingColor + "Ping: §f" + ping + "ms");
        }
        if (showCoords.isEnabled()) {
            lines.add(String.format("§7XYZ: §f%.1f / %.1f / %.1f",
                    player.getX(), player.getY(), player.getZ()));
        }
        if (showVelocity.isEnabled()) {
            lines.add(String.format("§7BPS: §f%.2f", bps));
        }
        if (showDimension.isEnabled()) {
            String dim = player.getWorld().getRegistryKey().getValue().getPath();
            lines.add("§7Dim: §f" + dim);
        }
        if (showBiome.isEnabled()) {
            net.minecraft.registry.entry.RegistryEntry<net.minecraft.world.biome.Biome> biomeEntry =
                    mc.world.getBiome(player.getBlockPos());
            String biomeName = biomeEntry.getKey()
                    .map(k -> k.getValue().getPath())
                    .orElse("unknown");
            lines.add("§7Biome: §f" + biomeName);
        }
        if (showLightLevel.isEnabled()) {
            int light = mc.world.getLightLevel(player.getBlockPos());
            String lightColor = light >= 8 ? "§a" : light >= 4 ? "§e" : "§c";
            lines.add(lightColor + "Light: §f" + light);
        }

        if (lines.isEmpty()) return;

        // Compute max text width for background
        int maxW = 0;
        for (String line : lines) {
            int w = mc.textRenderer.getWidth(net.minecraft.text.Text.of(line));
            if (w > maxW) maxW = w;
        }

        if (showBackground.isEnabled()) {
            ctx.fill(x - padX, y - padY,
                    x + maxW + padX, y + lines.size() * lineH + padY - 2,
                    0xAA111111);
        }

        for (String line : lines) {
            ctx.drawTextWithShadow(mc.textRenderer, net.minecraft.text.Text.of(line), x, y, 0xFFFFFFFF);
            y += lineH;
        }
    }

    private int getPing() {
        if (mc.player == null || mc.getNetworkHandler() == null) return 0;
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return entry != null ? entry.getLatency() : 0;
    }
}
