package ovh.excale.xkuhc.events.gamer;

import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.xkuhc.core.Gamer;

public class GamerDisconnectEvent extends GamerEvent {

	private static final HandlerList handlers = new HandlerList();

	public GamerDisconnectEvent(Gamer gamer, PlayerQuitEvent parentEvent) {
		super(gamer, parentEvent);

		parentEventCheck(event -> ((PlayerQuitEvent) event).getPlayer());

	}

	@Override
	public @Nullable PlayerQuitEvent getParentEvent() {
		return (PlayerQuitEvent) super.getParentEvent();
	}

	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}

}
