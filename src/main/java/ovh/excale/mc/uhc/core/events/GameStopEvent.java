package ovh.excale.mc.uhc.core.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ovh.excale.mc.uhc.Game;

public class GameStopEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final Game game;

	public GameStopEvent(Game game) {
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
