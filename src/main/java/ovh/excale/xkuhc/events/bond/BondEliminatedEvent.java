package ovh.excale.xkuhc.events.bond;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.core.Bond;

public class BondEliminatedEvent extends BondEvent {

	private static final HandlerList handlers = new HandlerList();

	public BondEliminatedEvent(Bond bond) {
		super(bond);
	}

	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}

}
