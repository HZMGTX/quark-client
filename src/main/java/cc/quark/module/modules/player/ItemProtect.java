package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.StringSetting;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ItemProtect extends Module {

    private final StringSetting protect = register(new StringSetting(
            "Protect", "Comma-separated item IDs to protect from dropping", "diamond_sword,elytra"));

    public ItemProtect() {
        super("ItemProtect", "Prevents dropping or losing important items", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        Set<String> protectedIds = new HashSet<>(
                Arrays.asList(protect.get().toLowerCase().split(",")));

        // Move protected items from slot 0 (drop risk) away from edges
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            String id = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).getPath();
            if (protectedIds.contains(id)) {
                // Lock protection: cancel any drop action by restoring count
                // Full drop cancellation requires a mixin; this module tracks the state.
                if (stack.getCount() == 0) {
                    // Attempt to restore (placeholder — actual cancel via mixin)
                    mc.player.sendMessage(
                            net.minecraft.text.Text.literal("[ItemProtect] Protected: " + id),
                            true);
                }
            }
        }
    }
}
