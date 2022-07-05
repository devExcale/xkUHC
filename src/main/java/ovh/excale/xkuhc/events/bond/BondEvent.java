package ovh.excale.xkuhc.events.bond;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.core.Bond;

@Getter
public abstract class BondEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	protected final Bond bond;

	protected BondEvent(Bond bond) {
		this(bond, false);
	}

	protected BondEvent(Bond bond, boolean async) {
		super(async);
		this.bond = bond;
	}

	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}

}
