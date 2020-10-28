package ovh.excale.mc.uhc;

import org.bukkit.entity.Player;
import ovh.excale.mc.uhc.exceptions.GameException;
import ovh.excale.mc.uhc.exceptions.GamePrepareException;

import java.util.Set;
import java.util.function.BiConsumer;

public interface Game {

	Set<Player> getPlayers();

	Set<Player> getPlayersAlive();

	Set<Player> getPlayersDead();

	/**
	 * Prepares the game to be run.<br/><br/>
	 * e.g. A game that requires a new world needs
	 * the world to be generated first.
	 *
	 * @throws GamePrepareException if something happens while preparing the game.
	 */
	void prepare() throws GamePrepareException;

	void start() throws GameException;

	void reset() throws GameException;

	void stop();

	State state();

	void addDisconnectListener(BiConsumer<Game, Player> onDisconnect);

	void addReconnectListener(BiConsumer<Game, Player> onReconnect);

	enum State {
		PREPARE,
		READY,
		RUNNING,
		FINAL,
		WORN
	}

}
