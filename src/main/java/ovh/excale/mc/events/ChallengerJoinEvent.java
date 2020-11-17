package ovh.excale.mc.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ovh.excale.mc.Challenger;

public class ChallengerJoinEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final Challenger challenger;

	public ChallengerJoinEvent(Challenger challenger) {
		this.challenger = challenger;
	}

	public Challenger getChallenger() {
		return challenger;
	}

	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
