package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ChatESP — highlights incoming chat messages that appear to originate from
 * players who are nearby, by re-echoing them with a colored prefix.
 */
public class ChatESP extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Highlight messages from players within this range (blocks)", 64.0, 10.0, 512.0));

    private final BoolSetting highlightAll = register(new BoolSetting(
            "Highlight All", "Highlight all chat regardless of proximity", false));

    private final IntSetting cooldownMs = register(new IntSetting(
            "Cooldown", "Min ms between duplicate alerts for the same player", 3000, 500, 10000));

    private final BoolSetting notifySound = register(new BoolSetting(
            "Sound", "Play a click sound when a nearby message is detected", true));

    private final Map<String, Long> lastNotify = new HashMap<>();

    public ChatESP() {
        super("ChatESP", "Highlights chat messages from nearby players", Category.RENDER);
    }

    @Override
    public void onDisable() {
        lastNotify.clear();
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        if (mc.player == null || mc.world == null) return;

        String raw = event.getMessage();

        // Collect names of nearby players
        Set<String> nearbyNames = new HashSet<>();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player) continue;
            if (!highlightAll.isEnabled() && mc.player.distanceTo(p) > range.get()) continue;
            nearbyNames.add(p.getGameProfile().getName());
        }

        if (nearbyNames.isEmpty() && !highlightAll.isEnabled()) return;

        // Check if the message looks like it's from a nearby player
        // Vanilla format: "<PlayerName> message"
        String matchedName = null;
        for (String name : nearbyNames) {
            if (raw.contains("<" + name + ">") || raw.startsWith(name + ": ")) {
                matchedName = name;
                break;
            }
        }

        if (matchedName == null && !highlightAll.isEnabled()) return;

        long now = System.currentTimeMillis();
        String key = matchedName != null ? matchedName : "__all__";
        Long lastTime = lastNotify.get(key);
        if (lastTime != null && now - lastTime < cooldownMs.get()) return;
        lastNotify.put(key, now);

        // Echo highlighted message back to local chat
        String display = "§e[NearbyChat] §f" + raw;
        mc.player.sendMessage(Text.literal(display), false);

        if (notifySound.isEnabled()) {
            mc.world.playSound(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundCategory.MASTER,
                    0.5f, 1.2f, false);
        }
    }
}
