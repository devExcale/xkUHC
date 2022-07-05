package ovh.excale.xkuhc.eventhandlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.core.GameAccessory;
import ovh.excale.xkuhc.core.GamePhase;
import ovh.excale.xkuhc.events.bond.BondCreateAsyncEvent;
import ovh.excale.xkuhc.events.bond.BondDeleteAsyncEvent;
import ovh.excale.xkuhc.events.bond.BondEliminatedEvent;
import ovh.excale.xkuhc.events.bond.BondSetColorAsyncEvent;

public class BondTabEnumerator implements Listener, GameAccessory {

	@Override
	public void enable() {

	}

	@Override
	public void disable() {

	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void onPhaseChange(@NotNull GamePhase phase) {

		switch(phase) {

			case READY -> enable();

			case STOPPED -> disable();

		}

	}

	@EventHandler
	private void onBondCreate(BondCreateAsyncEvent event) {


	}

	@EventHandler
	private void onBondDelete(BondDeleteAsyncEvent event) {


	}

	@EventHandler
	private void onBondEliminated(BondEliminatedEvent event) {

	}

	@EventHandler
	private void onBondSetColor(BondSetColorAsyncEvent event) {

	}

}
