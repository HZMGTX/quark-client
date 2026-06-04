package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GappleAlert extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to monitor enemy players", 16.0, 5.0, 64.0));

    private final BoolSetting sound = register(new BoolSetting(
            "Sound", "Play a sound when gapple is eaten", true));

    private final BoolSetting chat = register(new BoolSetting(
            "Chat", "Show chat message when gapple is eaten", true));

    private final Map<UUID, Boolean> wasEating = new HashMap<>();
    private final Map<UUID, Long> alertCooldowns = new HashMap<>();

    public GappleAlert() {
        super("GappleAlert", "Alerts when enemies eat golden apples", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        wasEating.clear();
        alertCooldowns.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity player)) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;

            UUID uuid = player.getUuid();

            // Check if eating a golden apple
            boolean eating = player.isUsingItem() &&
                    (player.getActiveItem().getItem() == Items.GOLDEN_APPLE ||
                     player.getActiveItem().getItem() == Items.ENCHANTED_GOLDEN_APPLE);

            boolean prevEating = wasEating.getOrDefault(uuid, false);

            // Detect start of eating
            if (eating && !prevEating) {
                Long lastAlert = alertCooldowns.get(uuid);
                if (lastAlert == null || now - lastAlert > 2000) {
                    String name = player.getGameProfile().getName();
                    boolean enchanted = player.getActiveItem().getItem() == Items.ENCHANTED_GOLDEN_APPLE;
                    String gappleType = enchanted ? "Notch Apple" : "Golden Apple";

                    if (chat.isEnabled()) {
                        ChatUtil.sendMessage("[GappleAlert] " + name + " is eating a " + gappleType + "!");
                    }
                    if (sound.isEnabled() && mc.world != null) {
                        mc.world.playSound(mc.player,
                                mc.player.getBlockPos(),
                                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                                net.minecraft.sound.SoundCategory.PLAYERS,
                                1.0f, 1.0f);
                    }
                    alertCooldowns.put(uuid, now);
                }
            }

            wasEating.put(uuid, eating);
        }
    }
}
