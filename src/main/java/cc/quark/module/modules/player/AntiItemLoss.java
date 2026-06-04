package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AntiItemLoss extends Module {

    private final BoolSetting keepInv = register(new BoolSetting(
            "KeepInv", "Log inventory before death for recovery", true));
    private final BoolSetting backup = register(new BoolSetting(
            "Backup", "Warn in chat when health drops below 4 hearts", true));

    private List<String> savedInventory = new ArrayList<>();
    private boolean warned = false;

    public AntiItemLoss() {
        super("AntiItemLoss", "Prevents losing items on death", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        warned = false;
        savedInventory.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        float health = mc.player.getHealth();

        if (backup.isEnabled() && health > 0 && health <= 8f && !warned) {
            warned = true;
            if (keepInv.isEnabled()) saveInventory();
            ChatUtil.warn("AntiItemLoss: Low health! Inventory backed up (" + savedInventory.size() + " slots).");
        }

        if (health > 8f) {
            warned = false;
        }

        if (keepInv.isEnabled() && health <= 0f && !savedInventory.isEmpty()) {
            ChatUtil.info("AntiItemLoss: Died. Backed-up items:");
            for (String line : savedInventory) {
                ChatUtil.info("  " + line);
            }
            savedInventory.clear();
        }
    }

    private void saveInventory() {
        savedInventory.clear();
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                savedInventory.add(i + ": " + stack.getName().getString() + " x" + stack.getCount());
            }
        }
    }
}
