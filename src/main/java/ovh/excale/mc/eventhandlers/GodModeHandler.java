package ovh.excale.mc.eventhandlers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import ovh.excale.mc.UHC;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GodModeHandler implements Listener {

	public Set<UUID> ids;

	public GodModeHandler() {
		ids = new HashSet<>();
	}

	public void setIds(Collection<UUID> ids) {
		this.ids.addAll(ids);
	}

	public void enableTime(long ticks) {

		enable();
		Bukkit.getScheduler()
				.runTaskLaterAsynchronously(UHC.instance(), this::disable, ticks);

	}

	public void enable() {

		Bukkit.getPluginManager()
				.registerEvents(this, UHC.instance());

	}

	public void disable() {

		EntityDamageEvent.getHandlerList()
				.unregister(this);

	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {

		Entity entity = event.getEntity();

		if(ids.contains(entity.getUniqueId()))
			event.setCancelled(true);

	}

}
