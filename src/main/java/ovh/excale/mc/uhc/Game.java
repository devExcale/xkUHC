package ovh.excale.mc.uhc;

import org.bukkit.entity.Player;
import ovh.excale.mc.uhc.exceptions.GameException;

import java.util.Set;

public interface Game {

	Set<Player> getPlayers();

	Set<Player> getPlayersAlive();

	Set<Player> getPlayersDead();

	void start() throws GameException;

	void reset() throws GameException;

	void stop();

	boolean isRunning();

	boolean isWorn();

}
