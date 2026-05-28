package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ShieldItem;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.Hand;

public class AutoShield extends Module {

    private final BoolSetting onlyProjectiles = register(new BoolSetting("Only Projectiles", "Only raise shield against projectiles", false));

    private final TimerUtil timer = new TimerUtil();
    private boolean shouldRaise = false;
    private float lastHealth = -1f;

    public AutoShield() {
        super("AutoShield", "Auto-raise shield when taking damage", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        shouldRaise = false;
        lastHealth = -1f;
        timer.reset();
    }

    @Override
    public void onDisable() {
        shouldRaise = false;
        lastHealth = -1f;
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (mc.player == null) return;
        if (onlyProjectiles.isEnabled()) {
            if (!event.getSource().isIn(DamageTypeTags.IS_PROJECTILE)) return;
        }
        shouldRaise = true;
        timer.reset();
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof EntityVelocityUpdateS2CPacket pkt)) return;
        if (pkt.getEntityId() != mc.player.getId()) return;
        if (onlyProjectiles.isEnabled()) return;
        shouldRaise = true;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        float currentHealth = mc.player.getHealth();
        if (lastHealth > 0 && currentHealth < lastHealth) {
            if (!onlyProjectiles.isEnabled()) {
                shouldRaise = true;
                timer.reset();
            }
        }
        lastHealth = currentHealth;

        Hand shieldHand = null;
        if (mc.player.getOffHandStack().getItem() instanceof ShieldItem) {
            shieldHand = Hand.OFF_HAND;
        } else if (mc.player.getMainHandStack().getItem() instanceof ShieldItem) {
            shieldHand = Hand.MAIN_HAND;
        }

        if (shieldHand == null) {
            shouldRaise = false;
            return;
        }

        if (shouldRaise && !mc.player.isUsingItem()) {
            mc.interactionManager.interactItem(mc.player, shieldHand);
        }

        if (timer.hasReached(1200)) {
            shouldRaise = false;
        }
    }
}
