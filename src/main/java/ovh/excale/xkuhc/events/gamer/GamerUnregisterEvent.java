package ovh.excale.xkuhc.events.gamer;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.core.Gamer;

public class GamerUnregisterEvent extends GamerEvent {

	private static final HandlerList handlers = new HandlerList();

	public GamerUnregisterEvent(Gamer gamer) {
		super(gamer);
	}

	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}

}
