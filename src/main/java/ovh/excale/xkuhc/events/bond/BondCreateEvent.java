package ovh.excale.xkuhc.events.bond;

import ovh.excale.xkuhc.core.Bond;

public class BondCreateEvent extends BondEvent {

	public BondCreateEvent(Bond bond) {
		super(bond);
	}

}
