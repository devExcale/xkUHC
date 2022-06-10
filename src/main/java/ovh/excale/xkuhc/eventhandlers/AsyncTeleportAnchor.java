package ovh.excale.xkuhc.eventhandlers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import ovh.excale.xkuhc.xkUHC;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AsyncTeleportAnchor implements Listener {

	private final Set<UUID> players;

	public AsyncTeleportAnchor() {
		players = Collections.synchronizedSet(new HashSet<>());
	}

	public void activate() {

	}

	public void deactivate() {

	}

	public AsyncTeleportAnchor waitFor(Player player) {

		players.add(player.getUniqueId());

		return this;
	}

	public AsyncTeleportAnchor waitFor(Player... players) {

		for(Player player : players)
			this.players.add(player.getUniqueId());

		return this;
	}

	public void await() {

		if(!players.isEmpty())
			synchronized(this) {

				Bukkit.getPluginManager()
						.registerEvents(this, xkUHC.instance());

				try {
					wait(20000L);
				} catch(InterruptedException ignored) {
				}

				PlayerTeleportEvent.getHandlerList()
						.unregister(this);

			}

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onTeleport(PlayerTeleportEvent event) {

		players.remove(event.getPlayer()
				.getUniqueId());

		if(players.isEmpty())
			notify();

	}

}
