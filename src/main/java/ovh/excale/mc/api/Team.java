package ovh.excale.mc.api;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface Team {

	boolean remove(Player player) throws IllegalStateException;

	boolean add(Player player) throws IllegalStateException;

	void unregister() throws IllegalStateException;

	void setColor(@Nullable ChatColor color) throws IllegalStateException;

	void setFriendlyFire(boolean friendlyFire) throws IllegalStateException;

	void broadcast(String message);

	@NotNull TeamedGame getGame();

	@NotNull String getName();

	@NotNull Set<Player> getMembers();

	@NotNull Set<Challenger> getChallengers();

	@NotNull Player[] getMembersAsArray();

	@NotNull Challenger[] getChallengersAsArray();

	@NotNull ChatColor getColor();

	boolean getFriendlyFire();

	boolean isEliminated();

	boolean isAlive();

}
