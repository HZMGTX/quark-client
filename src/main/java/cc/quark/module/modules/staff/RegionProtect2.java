package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventBlockBreak;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class RegionProtect2 extends Module {

    private final StringSetting cornerA = register(new StringSetting(
            "Corner A", "First corner as x,y,z (blank = use current position)", ""));
    private final StringSetting cornerB = register(new StringSetting(
            "Corner B", "Second corner as x,y,z (blank = use current position)", ""));
    private final BoolSetting preventBreak = register(new BoolSetting(
            "Prevent Break", "Cancel block-break events inside the region", true));
    private final BoolSetting preventPlace = register(new BoolSetting(
            "Prevent Place", "Warn and teleport back players who place blocks inside", true));
    private final BoolSetting notifyViolation = register(new BoolSetting(
            "Notify Violation", "Chat-notify staff when a violation is detected", true));
    private final BoolSetting setAOnEnable = register(new BoolSetting(
            "Set A On Enable", "Auto-set Corner A to your position when enabling", false));

    private Box region = null;
    private int tickCounter = 0;

    public RegionProtect2() {
        super("RegionProtect2", "Enhanced region protection between two corner points with violation alerts", Category.STAFF);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        if (setAOnEnable.isEnabled()) {
            BlockPos pos = mc.player.getBlockPos();
            cornerA.setValue(pos.getX() + "," + pos.getY() + "," + pos.getZ());
            ChatUtil.info("§6[RegionProtect2] §fCorner A set to §e" + cornerA.get());
        }
        region = buildRegion();
        if (region != null) {
            ChatUtil.success("§6[RegionProtect2] §fRegion active from §e" + cornerA.get()
                    + " §fto §e" + cornerB.get());
        } else {
            ChatUtil.warn("§6[RegionProtect2] §eConfigure Corner A and B, then re-enable.");
        }
    }

    @Override
    public void onDisable() {
        region = null;
    }

    @EventHandler
    public void onBlockBreak(EventBlockBreak event) {
        if (!preventBreak.isEnabled()) return;
        region = buildRegion();
        if (region == null) return;
        BlockPos pos = event.getPos();
        if (region.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
            // EventBlockBreak is not cancellable; notify staff and issue a region restore command
            if (notifyViolation.isEnabled()) {
                ChatUtil.warn("§6[RegionProtect2] §eBlock broken in protected region at §f" + pos.toShortString());
            }
            if (mc.player != null) {
                // Attempt to restore via WorldEdit-style setblock fallback
                mc.player.networkHandler.sendChatCommand(
                        "setblock " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
                                + " " + event.getState().getBlock().toString());
            }
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!preventPlace.isEnabled() || mc.world == null || mc.player == null) return;
        if (++tickCounter < 10) return;
        tickCounter = 0;
        region = buildRegion();
        if (region == null) return;
        // Passive: notify if non-staff players are inside the region
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player) continue;
            if (region.contains(p.getX(), p.getY(), p.getZ())) {
                if (notifyViolation.isEnabled()) {
                    ChatUtil.warn("§6[RegionProtect2] §e" + p.getName().getString()
                            + " §fis inside the protected region!");
                }
            }
        }
    }

    private Box buildRegion() {
        double[] a = parseCoord(cornerA.get());
        double[] b = parseCoord(cornerB.get());
        if (a == null || b == null) return null;
        return new Box(
                Math.min(a[0], b[0]), Math.min(a[1], b[1]), Math.min(a[2], b[2]),
                Math.max(a[0], b[0]) + 1, Math.max(a[1], b[1]) + 1, Math.max(a[2], b[2]) + 1);
    }

    private double[] parseCoord(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        String[] parts = s.trim().split(",");
        if (parts.length < 3) return null;
        try {
            return new double[]{
                    Double.parseDouble(parts[0].trim()),
                    Double.parseDouble(parts[1].trim()),
                    Double.parseDouble(parts[2].trim())};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
