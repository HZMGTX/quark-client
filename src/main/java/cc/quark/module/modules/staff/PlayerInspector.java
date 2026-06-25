package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;

public class PlayerInspector extends Module {
    private final BoolSetting showGear = register(new BoolSetting("Show Gear", "Show armor/weapon items", true));
    private final BoolSetting showHealth = register(new BoolSetting("Show Health", "Show health/hunger", true));
    private final BoolSetting showGamemode = register(new BoolSetting("Show Gamemode", "Show player gamemode", true));
    private final BoolSetting autoInspect = register(new BoolSetting("Auto Inspect", "Inspect on crosshair look", false));

    public PlayerInspector() {
        super("Player Inspector", "Inspect a player's gear, health, and gamemode", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.crosshairTarget == null) {
            ChatUtil.warn("[Inspector] Look at a player first (or enable Auto Inspect).");
            if (!autoInspect.isEnabled()) { disable(); return; }
            return;
        }
        if (mc.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() instanceof PlayerEntity p) {
            inspect(p);
        }
        if (!autoInspect.isEnabled()) disable();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!autoInspect.isEnabled() || mc.player == null) return;
        if (mc.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() instanceof PlayerEntity p) {
            inspect(p);
        }
    }

    private void inspect(PlayerEntity p) {
        ChatUtil.info("§6=== Inspecting: §f" + p.getName().getString() + " §6===");
        if (showHealth.isEnabled()) {
            ChatUtil.info("  §7HP: §f" + String.format("%.1f", p.getHealth()) + "/§f" + String.format("%.1f", p.getMaxHealth()));
        }
        if (showGamemode.isEnabled()) {
            boolean creative = p.getAbilities().creativeMode;
            boolean fly = p.getAbilities().allowFlying;
            ChatUtil.info("  §7Mode: §f" + (creative ? "Creative" : fly ? "Spectator-like" : "Survival/Adventure"));
        }
        if (showGear.isEnabled()) {
            for (var slot : net.minecraft.entity.EquipmentSlot.values()) {
                ItemStack s = p.getEquippedStack(slot);
                if (!s.isEmpty()) ChatUtil.info("  §7" + slot.getName() + ": §f" + s.getName().getString());
            }
        }
    }
}
