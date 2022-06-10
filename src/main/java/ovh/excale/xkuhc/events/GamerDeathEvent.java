package ovh.excale.xkuhc.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.core.Gamer;
import ovh.excale.xkuhc.core.GamerHub;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class GamerDeathEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final Gamer gamer;
	private final Gamer killer;
	private final DamageCause damageCause;

	public GamerDeathEvent(EntityDamageEvent event, GamerHub hub) {

		Player player = (Player) event.getEntity();

		gamer = hub.getGamer(player.getUniqueId());
		damageCause = event.getCause();

		Player killer = null;
		if(event instanceof EntityDamageByEntityEvent) {
			//
			Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
			if(damager instanceof Projectile) {
				ProjectileSource shooter = ((Projectile) damager).getShooter();
				if(shooter instanceof Player)
					killer = (Player) shooter;
			} else if(damager instanceof Player)
				killer = (Player) damager;

		}

		// TODO: FIX KILLER NULL IF KILLER IS PLAYER
		if(killer != null) {
			this.killer = hub.getGamer(killer.getUniqueId());
			this.killer.incrementKillCount();
		} else
			this.killer = null;

	}

	public Gamer getGamer() {
		return gamer;
	}

	public Gamer getKiller() {
		return killer;
	}

	public DamageCause getDamageCause() {
		return damageCause;
	}

	public boolean byGamer() {
		return killer != null;
	}

	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}