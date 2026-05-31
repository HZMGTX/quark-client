package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.*;

public class AntiBot2 extends Module {

    private final BoolSetting showCount = register(new BoolSetting("ShowCount", "Display blacklisted bot count in suffix", true));
    private final Set<UUID> blacklist = new HashSet<>();
    private final Map<UUID, Long> firstSeen = new HashMap<>();
    private final Map<UUID, int[]> positionHistory = new HashMap<>();

    public AntiBot2() {
        super("AntiBot2", "Detects and blacklists bot entities via unrealistic movement patterns", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        blacklist.clear();
        firstSeen.clear();
        positionHistory.clear();
    }

    @Override
    public String getSuffix() {
        return showCount.isEnabled() ? "Bots: " + blacklist.size() : null;
    }

    public boolean isBot(PlayerEntity entity) {
        return blacklist.contains(entity.getUuid());
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            UUID id = player.getUuid();

            firstSeen.putIfAbsent(id, System.currentTimeMillis());

            // Flag entities that appear with zero ping representation or identical position every tick
            positionHistory.putIfAbsent(id, new int[]{0, (int) player.getX(), (int) player.getZ()});
            int[] hist = positionHistory.get(id);
            int cx = (int) player.getX();
            int cz = (int) player.getZ();

            if (cx == hist[1] && cz == hist[2]) {
                hist[0]++;
            } else {
                hist[0] = 0;
                hist[1] = cx;
                hist[2] = cz;
            }

            // Bot heuristic: completely stationary for 10+ ticks after existing for >3s
            long age = System.currentTimeMillis() - firstSeen.get(id);
            if (hist[0] >= 10 && age > 3000 && !blacklist.contains(id)) {
                blacklist.add(id);
                ChatUtil.addMessage("Flagged bot: " + player.getGameProfile().getName());
            }
        }

        // Clean up disconnected players
        blacklist.removeIf(id -> mc.world.getPlayers().stream().noneMatch(p -> p.getUuid().equals(id)));
        firstSeen.keySet().removeIf(id -> mc.world.getPlayers().stream().noneMatch(p -> p.getUuid().equals(id)));
        positionHistory.keySet().removeIf(id -> mc.world.getPlayers().stream().noneMatch(p -> p.getUuid().equals(id)));
    }
}
