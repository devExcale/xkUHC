package ovh.excale.xkuhc.events.bond;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.core.Bond;
import ovh.excale.xkuhc.core.BondSnapshot;

public abstract class BondEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	protected final BondSnapshot bond;

	protected BondEvent(Bond bond) {
		this(bond, true);
	}

	protected BondEvent(Bond bond, boolean reference) {
		super(true);

		this.bond = new BondSnapshot(bond, reference);

	}

	public BondSnapshot getBond() {
		return bond;
	}

	public Bond getReferencedBond() {
		return bond.getReferencedBond();
	}

	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}

}
