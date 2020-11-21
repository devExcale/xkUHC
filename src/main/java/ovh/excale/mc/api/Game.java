package ovh.excale.mc.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

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

	@NotNull State getState();

	@NotNull UUID getAdminId();

	boolean isReady();

	void setAdmin(Player player);

	void setDisconnectListener(BiConsumer<Game, Player> onDisconnect);

	void setReconnectListener(BiConsumer<Game, Player> onReconnect);

	void resetListeners();

	enum State {

		PREPARE(true),
		READY(true),
		RUNNING(false),
		FINAL(false),
		WORN(false);

		private final boolean editable;

		State(boolean editable) {
			this.editable = editable;
		}

		public boolean isEditable() {
			return editable;
		}

	}

}
