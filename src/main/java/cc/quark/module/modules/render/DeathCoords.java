package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class DeathCoords extends Module {

    private final BoolSetting respawnAlert = register(new BoolSetting("Respawn Alert", "Announce death coords in chat on death", true));

    private final List<BlockPos> deathPositions = new ArrayList<>();

    private static final String[] DEATH_KEYWORDS = {
        "was slain", "was shot", "was blown up", "was killed", "fell from",
        "fell off", "fell into", "was squashed", "walked into", "drowned",
        "burned to death", "went up in flames", "was burned", "froze to death",
        "suffocated", "was poked", "was pricked", "hit the ground too hard",
        "tried to swim in lava", "died", "was struck by lightning",
        "was fireballed", "was doomed", "experienced kinetic energy",
        "was obliterated", "starved to death", "was impaled", "was stung"
    };

    public DeathCoords() {
        super("DeathCoords", "Detects death messages in chat and records coordinates", Category.RENDER);
    }

    @Override
    public void onEnable() {
        deathPositions.clear();
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        if (mc.player == null) return;

        String msg = event.getMessage();
        String playerName = mc.player.getName().getString();

        if (!msg.contains(playerName)) return;

        boolean isDeathMsg = false;
        for (String keyword : DEATH_KEYWORDS) {
            if (msg.contains(keyword)) {
                isDeathMsg = true;
                break;
            }
        }

        if (!isDeathMsg) return;

        BlockPos pos = mc.player.getBlockPos();
        deathPositions.add(0, pos);
        if (deathPositions.size() > 10) deathPositions.remove(deathPositions.size() - 1);

        if (respawnAlert.isEnabled()) {
            ChatUtil.info("Death coords: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        boolean isDead = mc.player.isDead() || mc.player.getHealth() <= 0;
        if (isDead && deathPositions.isEmpty()) {
            BlockPos pos = mc.player.getBlockPos();
            deathPositions.add(0, pos);
            if (respawnAlert.isEnabled()) {
                ChatUtil.info("Death coords: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
            }
        }

        if (deathPositions.isEmpty()) return;

        DrawContext ctx = event.getDrawContext();
        int y = 5;

        ctx.drawTextWithShadow(mc.textRenderer, "Death Coords:", 5, y, 0xFFFF5555);
        y += 10;
        for (int i = 0; i < deathPositions.size(); i++) {
            BlockPos pos = deathPositions.get(i);
            String text = (i == 0 ? "● " : (i + 1) + ". ") + pos.getX() + " " + pos.getY() + " " + pos.getZ();
            int col = i == 0 ? 0xFFFFFFFF : 0xFFAAAAAA;
            ctx.drawTextWithShadow(mc.textRenderer, text, 5, y, col);
            y += 10;
        }
    }
}
