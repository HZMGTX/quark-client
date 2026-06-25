package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;

public class AntiGrief extends Module {
    private final IntSetting breakThreshold = register(new IntSetting("Break Threshold", "Blocks/sec to flag as grief", 5, 1, 20));
    private final BoolSetting autoMute = register(new BoolSetting("Auto Mute", "Send /mute on detection", false));
    private final BoolSetting autoKick = register(new BoolSetting("Auto Kick", "Send /kick on detection", false));
    private final BoolSetting alertOnly = register(new BoolSetting("Alert Only", "Only show alert, no action", true));

    private final TimerUtil timer = new TimerUtil();

    public AntiGrief() {
        super("Anti Grief", "Detect and alert on potential grief activity", Category.STAFF, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(1000)) return;
        timer.reset();

        if (mc.world.getPlayers().size() < 2) return;

        for (var p : mc.world.getPlayers()) {
            if (p == mc.player) continue;
            String name = p.getName().getString();

            // Heuristic: player in creative mode digging fast is suspicious
            if (p.getAbilities().creativeMode && p.handSwingProgress > 0.5f) {
                ChatUtil.warn("[AntiGrief] §c" + name + " §fmay be griefing (creative mode active)!");
                if (!alertOnly.isEnabled()) {
                    if (autoMute.isEnabled()) mc.player.networkHandler.sendChatCommand("mute " + name + " 5m suspected grief");
                    if (autoKick.isEnabled()) mc.player.networkHandler.sendChatCommand("kick " + name + " suspected grief");
                }
            }
        }
    }
}
