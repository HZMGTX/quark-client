package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class AfkDuration extends Module {

    private final IntSetting afkThresholdSec = register(new IntSetting("AFKThreshold", "Seconds without movement to count as AFK", 5, 1, 60));

    private Vec3d lastPos    = null;
    private long  afkStart   = 0;
    private boolean isAfk    = false;
    private int  stillTicks  = 0;

    public AfkDuration() {
        super("AfkDuration", "Tracks how long you have been AFK and displays it in the action bar", Category.MISC);
    }

    @Override
    public void onEnable() {
        lastPos   = null;
        afkStart  = 0;
        isAfk     = false;
        stillTicks = 0;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal(""), true);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        Vec3d pos = mc.player.getPos();
        if (lastPos == null) {
            lastPos = pos;
            return;
        }

        boolean moved = pos.squaredDistanceTo(lastPos) > 0.001;
        lastPos = pos;

        if (moved) {
            stillTicks = 0;
            if (isAfk) {
                isAfk = false;
                mc.player.sendMessage(Text.literal(""), true);
            }
            return;
        }

        stillTicks++;
        int threshold = afkThresholdSec.get() * 20;

        if (!isAfk && stillTicks >= threshold) {
            isAfk    = true;
            afkStart = System.currentTimeMillis();
        }

        if (isAfk) {
            long seconds = (System.currentTimeMillis() - afkStart) / 1000;
            long mins = seconds / 60;
            long secs = seconds % 60;
            String msg = String.format("§7AFK: §e%02d:%02d", mins, secs);
            mc.player.sendMessage(Text.literal(msg), true);
        }
    }

    @Override
    public String getSuffix() {
        return isAfk ? "AFK" : "Active";
    }
}
