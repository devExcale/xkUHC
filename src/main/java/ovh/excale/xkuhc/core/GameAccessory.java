package ovh.excale.xkuhc.core;

import org.jetbrains.annotations.NotNull;

public interface GameAccessory {

	void enable();

	void disable();

	boolean isEnabled();

	void onPhaseChange(@NotNull GamePhase phase);

}
