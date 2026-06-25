package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * DamageIndicator — tracks entity health each tick and displays floating
 * damage numbers when an entity's health decreases.
 */
public class DamageIndicator extends Module {

    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "OnlyPlayers", "Only show damage indicators for players", false));

    private final BoolSetting showHealth = register(new BoolSetting(
            "ShowHealth", "Show current health alongside damage", true));

    /** Previous health per entity UUID */
    private final Map<UUID, Float> healthMap = new HashMap<>();

    /** Recent damage events: UUID -> (damage, display ticks remaining) */
    private final Map<UUID, float[]> damageDisplay = new HashMap<>();

    public DamageIndicator() {
        super("DamageIndicator", "Shows floating damage numbers when entities take damage", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        healthMap.clear();
        damageDisplay.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (onlyPlayers.isEnabled() && !(entity instanceof PlayerEntity)) continue;
            if (living.isRemoved() || living.getHealth() <= 0) continue;

            UUID id = entity.getUuid();
            float currentHealth = living.getHealth();

            if (healthMap.containsKey(id)) {
                float prevHealth = healthMap.get(id);
                float delta = prevHealth - currentHealth;
                if (delta > 0.1f) {
                    // Entity took damage — store it for rendering
                    damageDisplay.put(id, new float[]{delta, 40f}); // show for 40 ticks
                }
            }

            healthMap.put(id, currentHealth);
        }

        // Tick down display timers
        damageDisplay.entrySet().removeIf(entry -> {
            entry.getValue()[1]--;
            return entry.getValue()[1] <= 0;
        });

        // Remove dead entities from healthMap
        healthMap.keySet().removeIf(id -> {
            Entity e = java.util.stream.StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                    .filter(en -> en.getUuid().equals(id))
                    .findFirst().orElse(null);
            return e == null || (e instanceof LivingEntity le && le.isRemoved());
        });
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        for (Entity entity : mc.world.getEntities()) {
            UUID id = entity.getUuid();
            if (!damageDisplay.containsKey(id)) continue;
            float[] data = damageDisplay.get(id);
            float damage = data[0];
            float ticksLeft = data[1];

            // Float upward as ticks decrease
            float floatOffset = (40f - ticksLeft) * 0.05f;

            Vec3d entityPos = entity.getPos().add(0, entity.getHeight() + 0.5 + floatOffset, 0);
            Vec3d screenPos = entityPos.subtract(camPos);

            // Use Minecraft's text renderer in 3D world space
            // Draw via the matrix stack
            event.getMatrixStack().push();
            event.getMatrixStack().translate(screenPos.x, screenPos.y, screenPos.z);
            event.getMatrixStack().scale(-0.025f, -0.025f, 0.025f);

            String text = "-" + String.format("%.1f", damage);
            if (showHealth.isEnabled() && entity instanceof LivingEntity living) {
                text += " (" + String.format("%.1f", living.getHealth()) + ")";
            }

            // Render text using MC text renderer
            int color = 0xFFFF4444;
            mc.textRenderer.draw(text,
                    -mc.textRenderer.getWidth(text) / 2f, 0f, color,
                    true,
                    event.getMatrixStack().peek().getPositionMatrix(),
                    mc.getBufferBuilders().getEntityVertexConsumers(),
                    net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL,
                    0, 0xF000F0);
            mc.getBufferBuilders().getEntityVertexConsumers().draw();

            event.getMatrixStack().pop();
        }
    }
}
