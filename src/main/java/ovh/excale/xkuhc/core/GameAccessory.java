package ovh.excale.xkuhc.core;

import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.core.Game.Phase;

public interface GameAccessory {

	void enable();

	void disable();

	boolean isEnabled();

	void onPhaseChange(@NotNull Phase phase);

}
