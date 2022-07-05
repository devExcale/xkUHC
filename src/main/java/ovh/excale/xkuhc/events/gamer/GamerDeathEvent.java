package ovh.excale.xkuhc.events.gamer;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.xkuhc.core.Gamer;
import ovh.excale.xkuhc.core.GamerHub;

@Getter
public class GamerDeathEvent extends GamerEvent {

	private static final HandlerList handlers = new HandlerList();

	private final Gamer killer;
	private final DamageCause damageCause;

	public GamerDeathEvent(Gamer gamer, EntityDamageEvent parentEvent, GamerHub hub) throws IllegalArgumentException {
		super(gamer, parentEvent);

		parentEventCheck(event -> ((Player) ((EntityDamageEvent) event).getEntity()));

		damageCause = parentEvent.getCause();

		Gamer killer = null;

		if(parentEvent instanceof EntityDamageByEntityEvent pkEvent) {

			Entity damager = pkEvent.getEntity();

			if(damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter)
				killer = hub.getGamer(shooter.getUniqueId());
			else if(damager instanceof Player)
				killer = hub.getGamer(damager.getUniqueId());

			if(killer != null)
				killer.incrementKillCount();

		}

		this.killer = killer;

	}

	public boolean isPK() {
		return killer != null;
	}

	@Override
	public @Nullable EntityDamageEvent getParentEvent() {
		return (EntityDamageEvent) parentEvent;
	}

	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}

}
