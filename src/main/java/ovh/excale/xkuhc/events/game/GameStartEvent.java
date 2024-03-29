package ovh.excale.xkuhc.events.game;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.core.Game;

public class GameStartEvent extends GameEvent {

	private static final HandlerList handlers = new HandlerList();

	public GameStartEvent(Game game) {
		super(game);
	}

	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}

}
