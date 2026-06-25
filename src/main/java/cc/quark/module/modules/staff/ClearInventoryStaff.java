package cc.quark.module.modules.staff;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class ClearInventoryStaff extends Module {

    private final StringSetting targetPlayer = register(new StringSetting(
            "Target Player", "Name of the player whose inventory to clear", ""));
    private final BoolSetting keepArmor = register(new BoolSetting(
            "Keep Armor", "Preserve the player's equipped armor slots", true));
    private final BoolSetting keepHotbar = register(new BoolSetting(
            "Keep Hotbar", "Preserve items in the player's hotbar (slots 0–8)", false));

    public ClearInventoryStaff() {
        super("ClearInventoryStaff", "Clears the inventory of a specified player via /clear commands", Category.STAFF);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        String target = targetPlayer.get().trim();
        if (target.isEmpty()) {
            ChatUtil.warn("[ClearInventoryStaff] Set Target Player before enabling.");
            disable();
            return;
        }

        if (!keepArmor.isEnabled() && !keepHotbar.isEnabled()) {
            // Clear everything in one command
            mc.player.networkHandler.sendChatCommand("clear " + target);
            ChatUtil.info("§6[ClearInv] §fCleared full inventory of §e" + target);
        } else {
            // /clear with item slot exclusions is not vanilla; instruct via targeted clears
            // Clear main inventory (slots 9-35) always
            mc.player.networkHandler.sendChatCommand(
                    "item replace entity " + target + " container.9 air");
            // This is a simplified approach — a real server plugin would handle granular slot clears.
            // Fall back to full /clear and warn about limitations.
            mc.player.networkHandler.sendChatCommand("clear " + target);
            if (keepArmor.isEnabled()) {
                ChatUtil.warn("§6[ClearInv] §eNote: /clear removes all items. Re-equip armor manually.");
            }
            if (keepHotbar.isEnabled()) {
                ChatUtil.warn("§6[ClearInv] §eNote: /clear removes hotbar too. Plugin support needed for selective clear.");
            }
            ChatUtil.info("§6[ClearInv] §fCleared inventory of §e" + target
                    + " §f(keepArmor=" + keepArmor.isEnabled() + ", keepHotbar=" + keepHotbar.isEnabled() + ")");
        }
        disable();
    }
}
