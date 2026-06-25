package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * PvpTimer — tracks K/D ratio via chat detection and displays current
 * combat timer in the module suffix.
 * Also announces elapsed combat time every 5 seconds.
 */
public class PvPTimer extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Combat detection range", 6.0, 2.0, 20.0));

    private int combatTicks = 0;
    private int kills = 0;
    private int deaths = 0;

    // PvP kill/death message patterns (server-agnostic fragments)
    private static final String[] KILL_PATTERNS   = { " was slain by ", " was killed by ", " was shot by " };
    private static final String[] DEATH_PATTERNS  = { " died", " fell ", " drowned", " burned", " blew up" };

    public PvPTimer() {
        super("PvPTimer", "Tracks K/D ratio and combat timer via chat detection", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        combatTicks = 0;
        kills = 0;
        deaths = 0;
    }

    @Override
    public String getSuffix() {
        int secs = combatTicks / 20;
        return "K:" + kills + " D:" + deaths + " " + secs + "s";
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean inCombat = false;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof PlayerEntity)) continue;
            if (mc.player.distanceTo(entity) <= range.get()) { inCombat = true; break; }
        }

        if (inCombat) {
            combatTicks++;
            if (combatTicks % 100 == 0) {
                ChatUtil.info("[PvpTimer] In combat for " + (combatTicks / 20) + "s | K:" + kills + " D:" + deaths);
            }
        } else {
            combatTicks = 0;
        }
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (mc.player == null) return;
        String msg  = event.getMessage();
        String lower = msg.toLowerCase();
        String ownName = mc.player.getGameProfile().getName().toLowerCase();

        // Kill detection: we killed someone (our name appears before kill pattern)
        for (String pattern : KILL_PATTERNS) {
            if (lower.contains(pattern.toLowerCase())) {
                int idx = lower.indexOf(pattern.toLowerCase());
                String killer = lower.substring(0, idx).trim().replaceAll(".*\\s", "");
                if (killer.equals(ownName)) { kills++; break; }
            }
        }

        // Death detection: we died
        for (String pattern : DEATH_PATTERNS) {
            if (lower.startsWith(ownName) && lower.contains(pattern.toLowerCase())) {
                deaths++;
                break;
            }
        }
    }
}
