package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StaffESP extends Module {

    private final BoolSetting showGamemode = register(new BoolSetting(
            "Show Gamemode", "Show gamemode label above detected staff", true));

    private final BoolSetting showFlying = register(new BoolSetting(
            "Show Flying", "Highlight players with fly ability enabled", true));

    private final BoolSetting alertOnJoin = register(new BoolSetting(
            "Alert On Join", "Send chat alert when potential staff is detected", true));

    private final ColorSetting color = register(new ColorSetting(
            "Color", "ESP box color for detected staff", 0xFFFF3030));

    private final StringSetting nameList = register(new StringSetting(
            "Name List", "Comma-separated list of known staff names", ""));

    // Players currently tagged as potential staff
    private final List<PlayerEntity> detectedStaff = new ArrayList<>();
    // Track who we've already alerted about to avoid spam
    private final Set<String> alerted = new HashSet<>();

    public StaffESP() {
        super("StaffESP", "Highlight other staff members and hidden players.", Category.STAFF, 0);
    }

    @Override
    public void onDisable() {
        detectedStaff.clear();
        alerted.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;

        detectedStaff.clear();

        // Build name list set
        Set<String> knownNames = new HashSet<>();
        String raw = nameList.get().trim();
        if (!raw.isEmpty()) {
            for (String n : raw.split(",")) {
                String trimmed = n.trim();
                if (!trimmed.isEmpty()) knownNames.add(trimmed.toLowerCase());
            }
        }

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity player)) continue;

            boolean isStaff = false;

            // Check gamemode (non-survival = suspicious)
            if (mc.getNetworkHandler() != null) {
                var entry = mc.getNetworkHandler().getPlayerListEntry(player.getGameProfile().getId());
                if (entry != null) {
                    GameMode gm = entry.getGameMode();
                    if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR || gm == GameMode.ADVENTURE) {
                        isStaff = true;
                    }
                }
            }

            // Check name list
            String playerName = player.getGameProfile().getName().toLowerCase();
            if (!knownNames.isEmpty() && knownNames.contains(playerName)) {
                isStaff = true;
            }

            // Check fly ability
            if (showFlying.isEnabled() && player.getAbilities().allowFlying) {
                isStaff = true;
            }

            // Check invisibility
            if (player.isInvisible()) {
                isStaff = true;
            }

            if (isStaff) {
                detectedStaff.add(player);

                if (alertOnJoin.isEnabled()) {
                    String name = player.getGameProfile().getName();
                    if (!alerted.contains(name)) {
                        alerted.add(name);
                        ChatUtil.warn("[StaffESP] Potential staff detected: " + name);
                    }
                }
            }
        }

        // Remove from alerted set if the player left
        alerted.removeIf(name -> detectedStaff.stream()
                .noneMatch(p -> p.getGameProfile().getName().equals(name)));
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        if (detectedStaff.isEmpty()) return;

        MatrixStack matrices = event.getMatrixStack();
        float td = event.getTickDelta();

        float r = color.getRedF();
        float g = color.getGreenF();
        float b = color.getBlueF();
        float a = color.getAlphaF();

        for (PlayerEntity player : detectedStaff) {
            double ex = player.prevX + (player.getX() - player.prevX) * td;
            double ey = player.prevY + (player.getY() - player.prevY) * td;
            double ez = player.prevZ + (player.getZ() - player.prevZ) * td;

            Box box = player.getBoundingBox().offset(
                    ex - player.getX(), ey - player.getY(), ez - player.getZ());

            RenderUtil.drawESPBox(matrices, box, r, g, b, a, 1.5f);
        }
    }
}
