package ovh.excale.mc.core;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Gamer {

	@Nullable Bond getBond();

	@NotNull UUID getUniqueId();

	@NotNull Player getPlayer();

	void setAlive(boolean alive);

	boolean isAlive();

	boolean isOnline();

	boolean hasBond();

}
