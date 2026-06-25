package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

public class SignESP extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Max range to show signs through walls (blocks)", 16.0, 4.0, 64.0));

    private final ColorSetting color = register(new ColorSetting(
            "Color", "Sign text color", 0xFFFFFFFF));

    public SignESP() {
        super("SignESP", "Shows sign text through walls", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null || mc.gameRenderer == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int argb = color.get();
        double r = range.get();

        BlockPos playerPos = mc.player.getBlockPos();
        int chunkR = ((int) r >> 4) + 1;
        ChunkPos playerChunk = new ChunkPos(playerPos);

        for (int cx = playerChunk.x - chunkR; cx <= playerChunk.x + chunkR; cx++) {
            for (int cz = playerChunk.z - chunkR; cz <= playerChunk.z + chunkR; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof SignBlockEntity sign)) continue;
                    BlockPos pos = be.getPos();
                    if (mc.player.getPos().distanceTo(pos.toCenterPos()) > r) continue;

                    // Get sign text lines
                    List<String> textLines = new ArrayList<>();
                    for (int line = 0; line < 4; line++) {
                        Text text = sign.getFrontText().getMessage(line, false);
                        String str = text.getString().trim();
                        if (!str.isEmpty()) textLines.add(str);
                    }
                    if (textLines.isEmpty()) continue;

                    Vec3d worldPos = pos.toCenterPos().add(0, 0.5, 0);
                    int[] screen = worldToScreen(worldPos, sw, sh);
                    if (screen == null) continue;

                    int lineH = mc.textRenderer.fontHeight + 1;
                    int startY = screen[1] - (textLines.size() * lineH) / 2;

                    for (int i = 0; i < textLines.size(); i++) {
                        String line = textLines.get(i);
                        int tw = mc.textRenderer.getWidth(line);
                        ctx.drawTextWithShadow(mc.textRenderer, line,
                                screen[0] - tw / 2, startY + i * lineH, argb);
                    }
                }
            }
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
}
