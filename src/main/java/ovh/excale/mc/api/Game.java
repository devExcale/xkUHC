package ovh.excale.mc.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

// TODO: DEBUG MODE
public interface Game {

	Set<Player> getPlayers();

	Set<Player> getSpectators();

	/**
	 * Prepares the game to be run.<br/>
	 * <i>e.g.</i> A game that requires a new world needs
	 * the world to be generated first.
	 *
	 * @throws IllegalStateException if something happens while preparing the game.
	 */
	void prepare() throws IllegalStateException;

	void start() throws IllegalStateException;

	void reset() throws IllegalStateException;

	void stop();

	@NotNull Status getStatus();

	@NotNull UUID getAdminId();

	boolean isReady();

	void setAdmin(Player player);

	default Scoreboard getScoreboard() {
		//noinspection ConstantConditions
		return Bukkit.getScoreboardManager()
				.getMainScoreboard();
	}

	enum Status {

		PREPARE(true),
		READY(true),
		STARTING(false),
		RUNNING(false),
		FINAL(false),
		WORN(false);

		private final boolean editable;

		Status(boolean editable) {
			this.editable = editable;
		}

		public boolean isEditable() {
			return editable;
		}

	}

}
