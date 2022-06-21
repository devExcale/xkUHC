package ovh.excale.xkuhc.core;

import org.jetbrains.annotations.Nullable;

public class BondSnapshot extends Bond {

	private final Bond referencedBond;

	public BondSnapshot(Bond bond, boolean reference) {
		super(bond.getName(), bond.getGame());

		referencedBond = (reference) ? bond : null;

		setColor(bond.getColor());
		setFriendlyFire(bond.isFriendlyFireEnabled());

		getInternalGamersSet().addAll(bond.getInternalGamersSet());

	}

	public @Nullable Bond getReferencedBond() {
		return referencedBond;
	}

	// TODO: immutable

}
