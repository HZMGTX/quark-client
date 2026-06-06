package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.util.math.Box;

import java.util.List;

/**
 * EntityCleaner3 - Clears specific entity types from the client-visible world
 * by sending server kill commands via the kill entity selector.
 */
public class EntityCleaner3 extends Module {

    private final BoolSetting clearItems = register(new BoolSetting(
            "Clear Items", "Kill dropped item entities", true));

    private final BoolSetting clearMobs = register(new BoolSetting(
            "Clear Hostile Mobs", "Kill hostile mob entities", false));

    private final BoolSetting clearPassive = register(new BoolSetting(
            "Clear Passive", "Kill passive animal entities", false));

    private final BoolSetting clearArmorStands = register(new BoolSetting(
            "Clear Armor Stands", "Kill armor stand entities", false));

    private final StringSetting customType = register(new StringSetting(
            "Custom Type", "Extra entity type ID to clear (e.g. minecraft:bat)", ""));

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Radius (blocks) to scan for entities to clear", 32, 4, 128));

    private final IntSetting intervalSec = register(new IntSetting(
            "Interval (sec)", "Seconds between auto-clean passes", 10, 1, 300));

    private final BoolSetting notifyCount = register(new BoolSetting(
            "Notify Count", "Print count of entities cleared", true));

    private long lastClearMs = 0;

    public EntityCleaner3() {
        super("EntityCleaner3", "Clears specific entity types from the world via kill commands", Category.STAFF, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();
        if (now - lastClearMs < intervalSec.get() * 1000L) return;
        lastClearMs = now;

        performClean();
    }

    private void performClean() {
        int r = radius.get();
        Box area = mc.player.getBoundingBox().expand(r);

        List<Entity> entities = mc.world.getEntitiesByClass(Entity.class, area, e -> true);

        int cleared = 0;

        for (Entity e : entities) {
            if (e == mc.player) continue;

            boolean shouldKill = false;
            String typeId = null;

            if (clearItems.isEnabled() && e instanceof ItemEntity) {
                shouldKill = true;
                typeId = "minecraft:item";
            } else if (clearMobs.isEnabled() && isHostile(e)) {
                shouldKill = true;
                typeId = net.minecraft.registry.Registries.ENTITY_TYPE.getId(e.getType()).toString();
            } else if (clearPassive.isEnabled() && isPassive(e)) {
                shouldKill = true;
                typeId = net.minecraft.registry.Registries.ENTITY_TYPE.getId(e.getType()).toString();
            } else if (clearArmorStands.isEnabled() && e instanceof ArmorStandEntity) {
                shouldKill = true;
                typeId = "minecraft:armor_stand";
            } else {
                String custom = customType.get().trim();
                if (!custom.isEmpty()) {
                    String eTypeId = net.minecraft.registry.Registries.ENTITY_TYPE.getId(e.getType()).toString();
                    if (eTypeId.equalsIgnoreCase(custom)) {
                        shouldKill = true;
                        typeId = custom;
                    }
                }
            }

            if (shouldKill) {
                mc.player.networkHandler.sendChatCommand("kill " + e.getUuidAsString());
                cleared++;
            }
        }

        if (notifyCount.isEnabled() && cleared > 0) {
            ChatUtil.info("[EntityCleaner3] Cleared " + cleared + " entities.");
        }
    }

    private boolean isHostile(Entity e) {
        return e instanceof HostileEntity;
    }

    private boolean isPassive(Entity e) {
        return e instanceof PassiveEntity || e instanceof AnimalEntity;
    }
}
