package ovh.excale.xkuhc.eventhandlers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.core.Game.Phase;
import ovh.excale.xkuhc.core.GameAccessory;
import ovh.excale.xkuhc.xkUHC;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GodModeHandler implements Listener, GameAccessory {

	public Set<UUID> ids;
	private boolean enabled;

	public GodModeHandler() {
		ids = new HashSet<>();
	}

	public void setIds(Collection<UUID> ids) {
		this.ids.addAll(ids);
	}

	public void enableTime(long ticks) {

		enable();
		Bukkit.getScheduler()
				.runTaskLaterAsynchronously(xkUHC.instance(), this::disable, ticks);

	}

	@Override
	public void enable() {

		Bukkit.getPluginManager()
				.registerEvents(this, xkUHC.instance());

		enabled = true;

	}

	@Override
	public void disable() {

		EntityDamageEvent.getHandlerList()
				.unregister(this);

		enabled = false;

	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void onPhaseChange(@NotNull Phase phase) {

		switch(phase) {

			case STARTING, ENDING -> enable();

			case RUNNING -> enableTime(200L);

			case STOPPED -> disable();

		}

	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {

		Entity entity = event.getEntity();

		if(ids.contains(entity.getUniqueId()))
			event.setCancelled(true);

	}

}
