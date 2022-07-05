package ovh.excale.xkuhc.events.bond;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.core.Bond;

public class BondDeleteAsyncEvent extends BondEvent {

	private static final HandlerList handlers = new HandlerList();

	public BondDeleteAsyncEvent(Bond bond) {
		super(bond, true);
	}

	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}

}
