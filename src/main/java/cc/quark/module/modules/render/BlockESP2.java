package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockESP2 extends Module {

    private final StringSetting blocks = register(new StringSetting(
            "Blocks", "Comma-separated block IDs to highlight (e.g. diamond_ore,emerald_ore)",
            "diamond_ore,emerald_ore"));

    private final ColorSetting color = register(new ColorSetting(
            "Color", "ESP overlay color (ARGB)", 0xFF00FFFF));

    private final List<BlockPos> found = new ArrayList<>();
    private int scanTimer = 0;
    private static final int SCAN_RADIUS = 24;
    private static final int SCAN_INTERVAL = 20;

    public BlockESP2() {
        super("BlockESP2", "Enhanced block ESP with multiple block types", Category.RENDER);
    }

    @Override
    public void onEnable() {
        found.clear();
        scanTimer = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (--scanTimer > 0) return;
        scanTimer = SCAN_INTERVAL;

        List<String> targets = Arrays.asList(blocks.get().split(","));
        found.clear();

        BlockPos center = mc.player.getBlockPos();
        int r = SCAN_RADIUS;

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            BlockState state = mc.world.getBlockState(pos);
            if (state.isAir()) continue;
            String id = Registries.BLOCK.getId(state.getBlock()).getPath();
            if (targets.stream().anyMatch(t -> t.trim().equalsIgnoreCase(id))) {
                found.add(pos.toImmutable());
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.gameRenderer == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int argb = color.get();

        for (BlockPos pos : found) {
            Vec3d center = pos.toCenterPos();
            int[] screen = worldToScreen(center, sw, sh);
            if (screen == null) continue;
            ctx.fill(screen[0] - 2, screen[1] - 2, screen[0] + 2, screen[1] + 2, argb);
        }
    }

    private int[] worldToScreen(Vec3d world, int sw, int sh) {
        net.minecraft.client.render.Camera cam = mc.gameRenderer.getCamera();
        Vec3d rel = world.subtract(cam.getPos());
        double fov = Math.toRadians(mc.options.getFov().getValue());
        float yaw = (float) Math.toRadians(cam.getYaw());
        float pitch = (float) Math.toRadians(cam.getPitch());

        double cosYaw = Math.cos(yaw), sinYaw = Math.sin(yaw);
        double cosPitch = Math.cos(pitch), sinPitch = Math.sin(pitch);

        double rx = rel.x * cosYaw - rel.z * sinYaw;
        double ry = rel.y * cosPitch - (rel.x * sinYaw + rel.z * cosYaw) * sinPitch;
        double rz = rel.y * sinPitch + (rel.x * sinYaw + rel.z * cosYaw) * cosPitch;

        if (rz <= 0) return null;
        double scale = (sw / 2.0) / Math.tan(fov / 2.0);
        int sx = (int) (sw / 2.0 + rx * scale / rz);
        int sy = (int) (sh / 2.0 - ry * scale / rz);
        if (sx < 0 || sx > sw || sy < 0 || sy > sh) return null;
        return new int[]{sx, sy};
    }

    @Override
    public String getSuffix() {
        return found.size() + " blocks";
    }
}
