package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerJail extends Module {

    private final StringSetting targetPlayer = register(new StringSetting(
            "Target Player", "Name of the player to jail", ""));
    private final IntSetting jailX = register(new IntSetting(
            "Jail X", "X coordinate of the jail location", 0, -30_000_000, 30_000_000));
    private final IntSetting jailY = register(new IntSetting(
            "Jail Y", "Y coordinate of the jail location", 64, -64, 320));
    private final IntSetting jailZ = register(new IntSetting(
            "Jail Z", "Z coordinate of the jail location", 0, -30_000_000, 30_000_000));
    private final BoolSetting preventEscape = register(new BoolSetting(
            "Prevent Escape", "Re-teleport the player back every tick if they move", true));

    private int tickCounter = 0;
    private static final int RETELEPORT_INTERVAL = 20; // re-tp every second

    public PlayerJail() {
        super("PlayerJail", "Teleports a player to the saved jail coords and prevents escape", Category.STAFF);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        String target = targetPlayer.get().trim();
        if (target.isEmpty()) {
            ChatUtil.warn("[PlayerJail] Set Target Player before enabling.");
            disable();
            return;
        }
        teleportToJail(target);
        tickCounter = 0;
        ChatUtil.info("§6[PlayerJail] §fJailed §e" + target
                + " §fat §e" + jailX.get() + "," + jailY.get() + "," + jailZ.get());
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!preventEscape.isEnabled()) return;
        if (mc.player == null) return;
        if (++tickCounter < RETELEPORT_INTERVAL) return;
        tickCounter = 0;

        String target = targetPlayer.get().trim();
        if (target.isEmpty()) return;

        // Check if the target player has drifted from jail position
        if (mc.world != null) {
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (p.getName().getString().equalsIgnoreCase(target)) {
                    double dx = p.getX() - jailX.get();
                    double dz = p.getZ() - jailZ.get();
                    double dy = p.getY() - jailY.get();
                    if (Math.abs(dx) > 1 || Math.abs(dy) > 1 || Math.abs(dz) > 1) {
                        teleportToJail(target);
                    }
                    break;
                }
            }
        }
    }

    private void teleportToJail(String target) {
        if (mc.player == null) return;
        mc.player.networkHandler.sendChatCommand(
                "tp " + target + " " + jailX.get() + " " + jailY.get() + " " + jailZ.get());
    }
}
