package ovh.excale.xkuhc.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.core.Game;

public class GameStartEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final Game game;

	public GameStartEvent(Game game) {
		super(true);
		this.game = game;
	}

	public Game getGame() {
		return game;
	}

	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
