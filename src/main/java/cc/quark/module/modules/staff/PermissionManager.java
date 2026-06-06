package cc.quark.module.modules.staff;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.setting.BoolSetting;
import cc.quark.module.setting.StringSetting;
import net.minecraft.text.Text;

import java.util.*;

public class PermissionManager extends Module {

    private final StringSetting adminPlayers   = new StringSetting("Admins", "");
    private final StringSetting modPlayers     = new StringSetting("Mods", "");
    private final StringSetting helperPlayers  = new StringSetting("Helpers", "");
    private final BoolSetting showInName       = new BoolSetting("ShowInName", true);
    private final BoolSetting chatPrefix       = new BoolSetting("ChatPrefix", true);

    private final Map<String, String> permCache = new HashMap<>();

    public PermissionManager() {
        super("PermissionManager", "Assigns custom permission roles to players (admin/mod/helper)", Category.STAFF);
        addSettings(adminPlayers, modPlayers, helperPlayers, showInName, chatPrefix);
    }

    @Override
    public void onEnable() {
        Quark.mc.getEventBus().subscribe(this);
        rebuildCache();
    }

    @Override public void onDisable() { Quark.mc.getEventBus().unsubscribe(this); }

    private void rebuildCache() {
        permCache.clear();
        for (String p : adminPlayers.get().split(",")) if (!p.isBlank()) permCache.put(p.trim(), "Admin");
        for (String p : modPlayers.get().split(","))   if (!p.isBlank()) permCache.put(p.trim(), "Mod");
        for (String p : helperPlayers.get().split(","))if (!p.isBlank()) permCache.put(p.trim(), "Helper");
    }

    public String getRole(String playerName) {
        return permCache.getOrDefault(playerName, "User");
    }

    public boolean isAdmin(String playerName)  { return "Admin".equals(permCache.get(playerName)); }
    public boolean isMod(String playerName)    { return "Mod".equals(permCache.get(playerName)); }

    @EventHandler
    public void onTick(EventTick event) {
        // Rebuild cache every 200 ticks in case settings changed
        if (Quark.mc != null && Quark.mc.player != null && Quark.mc.player.age % 200 == 0) {
            rebuildCache();
        }
    }
}
