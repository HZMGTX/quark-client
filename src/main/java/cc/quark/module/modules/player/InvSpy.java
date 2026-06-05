package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * InvSpy - shows the visible equipment/inventory of nearby players.
 *
 * Note: In vanilla Minecraft, the server only sends equipment slots (armor + hands)
 * to nearby clients. This module displays those visible slots on a HUD or in chat.
 * Full inventory contents are server-controlled and are not available client-side
 * unless the server sends them (e.g. via mods or specific plugins).
 */
public class InvSpy extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to spy on player inventories (blocks)", 10.0, 1.0, 64.0));

    private final BoolSetting showOnHUD = register(new BoolSetting(
            "ShowOnHUD", "Display inventory info on the HUD overlay", true));

    /** Public list of spy entries rendered by a HUD component. */
    public static final List<String> hudLines = new ArrayList<>();

    private int tickCooldown = 0;

    public InvSpy() {
        super("InvSpy", "Shows inventory contents of nearby players", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        hudLines.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        tickCooldown++;
        if (tickCooldown < 20) return; // Update once per second
        tickCooldown = 0;

        double r = range.get();
        double rSq = r * r;

        java.util.List<net.minecraft.client.network.AbstractClientPlayerEntity> nearby = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player)
                .filter(p -> p.squaredDistanceTo(mc.player) <= rSq)
                .sorted(Comparator.comparingDouble(p -> p.squaredDistanceTo(mc.player)))
                .toList();

        hudLines.clear();

        for (PlayerEntity player : nearby) {
            StringBuilder sb = new StringBuilder();
            sb.append(player.getName().getString()).append(": ");

            // Equipment slots visible to clients
            for (net.minecraft.entity.EquipmentSlot slot : net.minecraft.entity.EquipmentSlot.values()) {
                ItemStack stack = player.getEquippedStack(slot);
                if (!stack.isEmpty()) {
                    sb.append(stack.getName().getString()).append(" ");
                }
            }

            String line = sb.toString().trim();
            hudLines.add(line);

            if (!showOnHUD.isEnabled()) {
                ChatUtil.info("[InvSpy] " + line);
            }
        }
    }
}
