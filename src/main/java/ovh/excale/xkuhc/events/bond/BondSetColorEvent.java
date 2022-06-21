package ovh.excale.xkuhc.events.bond;

import org.bukkit.ChatColor;
import ovh.excale.xkuhc.core.Bond;

public class BondSetColorEvent extends BondEvent {

	public BondSetColorEvent(Bond bond) {
		super(bond);
	}

	public ChatColor getColor() {
		return bond.getColor();
	}

}
