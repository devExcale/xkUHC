package ovh.excale.mc.core;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Basically, a team.
 */
public interface Bond {

	String getName();

	/**
	 * Adds a {@link Gamer gamer} to a {@link Bond bond}
	 *
	 * @param gamer The gamer to add to this bond
	 * @throws IllegalStateException    if the bond is frozen
	 * @throws IllegalArgumentException if gamer has already bonded or is null
	 * @see Bond#freeze()
	 * @see Gamer#hasBond()
	 */
	void boundGamer(@NotNull Gamer gamer) throws IllegalStateException, IllegalArgumentException;

	/**
	 * Unbounds a {@link Gamer gamer} from a {@link Bond bond}
	 *
	 * @param gamer The gamer to unbound
	 * @throws IllegalStateException    if the bond is frozen
	 * @throws IllegalArgumentException if the gamer is not bound or is null
	 * @see Bond#freeze()
	 * @see Gamer#hasBond()
	 */
	void unboundGamer(@NotNull Gamer gamer) throws IllegalStateException, IllegalArgumentException;

	/**
	 * Get all the gamers in the bond
	 *
	 * @return a {@link Set} with all the gamers in the bond
	 */
	Set<? extends Gamer> getGamers();

	Game getGame();

	/**
	 * Freezes a bond<br>
	 * (i.e. won't take further changes)
	 *
	 * @throws IllegalStateException if the bond is already frozen
	 */
	void freeze() throws IllegalStateException;

	/**
	 * Checks whether the bond is frozen
	 *
	 * @return true if the bond if frozen, false if not
	 */
	boolean isFrozen();

	/**
	 * Broadcasts a message to all gamers in the bond
	 *
	 * @param message the message to broadcast. If the message is null, the method won't do anything
	 */
	void broadcast(String message);

	/**
	 * Sets the bond's color
	 *
	 * @param color The {@link ChatColor color} to set
	 * @throws IllegalStateException if the bond is frozen
	 */
	void setColor(ChatColor color) throws IllegalStateException;

	ChatColor getColor();

	void setFriendlyFire(boolean friendlyFire);

	boolean isFriendlyFire();

}
