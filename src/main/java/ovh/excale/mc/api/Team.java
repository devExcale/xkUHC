package ovh.excale.mc.api;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface Team {

	boolean remove(Player player);

	boolean add(Player player);

	void unregister();

	void setColor(ChatColor color);

	void setFriendlyFire(boolean friendlyFire);

	@NotNull String getName();

	@NotNull Set<Player> getMembers();

	@NotNull ChatColor getColor();

	boolean getFriendlyFire();

	boolean isEliminated();

	boolean isAlive();

}
