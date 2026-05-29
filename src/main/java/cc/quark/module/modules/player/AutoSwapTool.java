package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.InventoryUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoSwapTool extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Which tool to equip",
            "All", "All", "Pickaxe", "Axe", "Shovel"));
    private final BoolSetting onlyWhenBreaking = register(new BoolSetting(
            "Only Breaking", "Only swap when actively breaking a block", true));

    private int prevSlot = -1;

    public AutoSwapTool() {
        super("AutoSwapTool", "Automatically equips the best tool for the targeted block", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        if (prevSlot >= 0 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.crosshairTarget == null) return;
        if (mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            // Not looking at block — restore previous slot
            if (prevSlot >= 0) {
                mc.player.getInventory().selectedSlot = prevSlot;
                prevSlot = -1;
            }
            return;
        }

        if (onlyWhenBreaking.isEnabled() && !mc.options.attackKey.isPressed()) return;

        int bestSlot = switch (mode.get()) {
            case "Pickaxe" -> InventoryUtil.findBestPickaxe();
            case "Axe"     -> InventoryUtil.findBestAxe();
            case "Shovel"  -> findBestShovel();
            default        -> findBestToolForBlock();  // "All"
        };

        if (bestSlot >= 0 && bestSlot < 9) {
            if (prevSlot < 0) prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }

    private int findBestShovel() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof net.minecraft.item.ShovelItem) return i;
        }
        return -1;
    }

    private int findBestToolForBlock() {
        // Try best pickaxe first, then axe, then shovel
        int s = InventoryUtil.findBestPickaxe();
        if (s >= 0 && s < 9) return s;
        s = InventoryUtil.findBestAxe();
        if (s >= 0 && s < 9) return s;
        return findBestShovel();
    }
}
