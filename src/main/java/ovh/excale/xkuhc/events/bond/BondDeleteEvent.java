package ovh.excale.xkuhc.events.bond;

import ovh.excale.xkuhc.core.Bond;

public class BondDeleteEvent extends BondEvent {

	public BondDeleteEvent(Bond bond) {
		super(bond, false);
	}

}
