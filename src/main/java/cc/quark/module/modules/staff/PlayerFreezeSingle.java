package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class PlayerFreezeSingle extends Module {

    private final StringSetting targetPlayer = register(new StringSetting(
            "Target Player", "Name of the player to freeze in place", ""));
    private final IntSetting autoUnfreezeAfter = register(new IntSetting(
            "Auto-Unfreeze (Seconds)", "Seconds until auto-unfreeze (0 = never)", 0, 0, 300));

    private Vec3d freezePos = null;
    private int elapsed = 0; // ticks since freeze started

    public PlayerFreezeSingle() {
        super("PlayerFreezeSingle", "Freezes a single named player by repeatedly teleporting them to their freeze point", Category.STAFF);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        String target = targetPlayer.get().trim();
        if (target.isEmpty()) {
            ChatUtil.warn("[FreezeSingle] Set Target Player before enabling.");
            disable();
            return;
        }

        // Capture freeze position from current world state
        freezePos = null;
        elapsed = 0;

        if (mc.world != null) {
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (p.getName().getString().equalsIgnoreCase(target)) {
                    freezePos = p.getPos();
                    break;
                }
            }
        }

        if (freezePos == null) {
            // Player may not be in render distance; freeze at a default and warn
            ChatUtil.warn("[FreezeSingle] §e" + target + " §fnot visible; using /tp-based freeze.");
        }

        // Issue server-side freeze command (plugin) as primary method
        mc.player.networkHandler.sendChatCommand("freeze " + target);
        ChatUtil.info("§6[FreezeSingle] §fFreezing §e" + target);
    }

    @Override
    public void onDisable() {
        String target = targetPlayer.get().trim();
        if (!target.isEmpty() && mc.player != null) {
            mc.player.networkHandler.sendChatCommand("unfreeze " + target);
            ChatUtil.info("§6[FreezeSingle] §fUnfroze §e" + target);
        }
        freezePos = null;
        elapsed = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        elapsed++;

        // Auto-unfreeze timer
        int limit = autoUnfreezeAfter.get();
        if (limit > 0 && elapsed >= limit * 20) {
            ChatUtil.info("§6[FreezeSingle] §fAuto-unfreeze triggered after §e" + limit + "s.");
            disable();
            return;
        }

        // Every 20 ticks: re-teleport the target back to freeze position if they moved
        if (elapsed % 20 != 0 || freezePos == null || mc.world == null) return;

        String target = targetPlayer.get().trim();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p.getName().getString().equalsIgnoreCase(target)) {
                double dist = p.getPos().distanceTo(freezePos);
                if (dist > 0.5) {
                    mc.player.networkHandler.sendChatCommand(
                            "tp " + target + " " + freezePos.x + " " + freezePos.y + " " + freezePos.z);
                }
                break;
            }
        }
    }
}
