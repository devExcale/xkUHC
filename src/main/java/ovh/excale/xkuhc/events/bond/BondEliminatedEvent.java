package ovh.excale.xkuhc.events.bond;

import ovh.excale.xkuhc.core.Bond;

public class BondEliminatedEvent extends BondEvent {

	public BondEliminatedEvent(Bond bond) {
		super(bond, true);
	}

}
