package ovh.excale.xkuhc.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.core.Gamer;
import ovh.excale.xkuhc.core.GamerHub;

public class GamerDisconnectEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final PlayerQuitEvent playerEvent;
	private final GamerHub hub;
	private final Gamer gamer;

	public GamerDisconnectEvent(PlayerQuitEvent playerEvent, GamerHub hub) {
		this.playerEvent = playerEvent;
		this.hub = hub;

		gamer = hub.getGamer(playerEvent.getPlayer()
				.getUniqueId());

	}

	public PlayerQuitEvent getPlayerEvent() {
		return playerEvent;
	}

	public GamerHub getHub() {
		return hub;
	}

	public Gamer getGamer() {
		return gamer;
	}

	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
