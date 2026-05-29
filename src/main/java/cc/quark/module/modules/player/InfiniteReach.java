package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;

public class InfiniteReach extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Which reach distance to extend",
            "Both", "Block", "Entity", "Both"));
    private final DoubleSetting extraReach = register(new DoubleSetting(
            "Extra Reach", "Additional reach blocks to add", 3.0, 0.5, 10.0));

    private static final Identifier BLOCK_MOD_ID  = Identifier.of("quark", "infinite_reach_block");
    private static final Identifier ENTITY_MOD_ID = Identifier.of("quark", "infinite_reach_entity");

    public InfiniteReach() {
        super("InfiniteReach", "Extends block and/or entity interaction reach distance", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        removeModifiers();
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;

        boolean doBlock  = mode.is("Block") || mode.is("Both");
        boolean doEntity = mode.is("Entity") || mode.is("Both");
        double extra = extraReach.get();

        if (doBlock) {
            var attr = mc.player.getAttributeInstance(EntityAttributes.BLOCK_INTERACTION_RANGE);
            if (attr != null) {
                attr.removeModifier(BLOCK_MOD_ID);
                attr.addTemporaryModifier(new EntityAttributeModifier(
                        BLOCK_MOD_ID, extra, EntityAttributeModifier.Operation.ADD_VALUE));
            }
        }

        if (doEntity) {
            var attr = mc.player.getAttributeInstance(EntityAttributes.ENTITY_INTERACTION_RANGE);
            if (attr != null) {
                attr.removeModifier(ENTITY_MOD_ID);
                attr.addTemporaryModifier(new EntityAttributeModifier(
                        ENTITY_MOD_ID, extra, EntityAttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    private void removeModifiers() {
        if (mc.player == null) return;
        var blockAttr = mc.player.getAttributeInstance(EntityAttributes.BLOCK_INTERACTION_RANGE);
        if (blockAttr != null) blockAttr.removeModifier(BLOCK_MOD_ID);
        var entityAttr = mc.player.getAttributeInstance(EntityAttributes.ENTITY_INTERACTION_RANGE);
        if (entityAttr != null) entityAttr.removeModifier(ENTITY_MOD_ID);
    }

    @Override
    public String getSuffix() {
        return "+" + extraReach.get() + " " + mode.get();
    }
}
