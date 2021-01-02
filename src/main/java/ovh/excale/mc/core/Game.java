package ovh.excale.mc.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public interface Game {

	/**
	 * Register a {@link Player player} in a {@link Game game}
	 *
	 * @param player The player to register
	 * @return The player registered as a {@link Gamer gamer}
	 * @throws IllegalStateException    if the game is frozen
	 * @throws IllegalArgumentException if the player is already registered, or if null
	 */
	@NotNull Gamer register(@NotNull Player player) throws IllegalStateException, IllegalArgumentException;

	@Nullable Gamer getGamer(@Nullable Player player);

	Set<? extends Gamer> getGamers();

	Set<Player> getPlayers();

	@NotNull BondManager getBondManager();

	void broadcast(String message);

	void tryStart() throws IllegalStateException;

	void unset() throws IllegalStateException;

	void stop() throws IllegalStateException;

	void freeze() throws IllegalStateException;

	void reloadConfig() throws IllegalStateException;

	boolean isFrozen();

	@NotNull Map<String, String> dump();

	default @NotNull Scoreboard getScoreboard() {
		//noinspection ConstantConditions
		return Bukkit.getScoreboardManager()
				.getMainScoreboard();
	}

}
