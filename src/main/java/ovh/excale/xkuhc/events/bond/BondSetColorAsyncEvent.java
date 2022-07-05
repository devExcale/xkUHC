package ovh.excale.xkuhc.events.bond;

import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.core.Bond;

public class BondSetColorAsyncEvent extends BondEvent {

	private static final HandlerList handlers = new HandlerList();

	public BondSetColorAsyncEvent(Bond bond) {
		super(bond, true);
	}

	public ChatColor getColor() {
		return bond.getColor();
	}

	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}

}
