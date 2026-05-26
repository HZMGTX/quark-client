package cc.quark.event.events;
import cc.quark.event.Event;
import net.minecraft.entity.damage.DamageSource;
public class EventDamage extends Event {
    private float amount;
    private final DamageSource source;
    public EventDamage(float amount, DamageSource source) { this.amount = amount; this.source = source; }
    public float getAmount() { return amount; }
    public void setAmount(float amount) { this.amount = amount; }
    public DamageSource getSource() { return source; }
    @Override public boolean isCancellable() { return true; }
}
