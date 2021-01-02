package ovh.excale.mc.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface BondManager {

	/**
	 * Gets the game this manager is associated with.
	 *
	 * @return The game this manager manages
	 */
	@NotNull Game getGame();

	/**
	 * Creates a new {@link Bond bond}
	 *
	 * @param bondName The name of the new bond
	 * @return The new {@link Bond bond}
	 * @throws IllegalStateException    if the {@link BondManager manager} is frozen
	 * @throws IllegalArgumentException if a bond with the provided name already exists, or if the bondName is null
	 */
	@NotNull Bond createBond(@NotNull String bondName) throws IllegalStateException, IllegalArgumentException;

	/**
	 * Unregisters a bond, freeing all the {@link Gamer gamers} in the bond
	 *
	 * @param bondName The name of the bond to unregister
	 * @throws IllegalStateException    if the {@link BondManager manager} is frozen
	 * @throws IllegalArgumentException if a bond with the specified name doesn't exist, or if bondName is null
	 */
	void breakBond(@NotNull String bondName) throws IllegalStateException, IllegalArgumentException;

	/**
	 * Gets a {@link Bond}.
	 *
	 * @param bondName the name of the bond
	 * @return The bond if found, or null otherwise. A null bondName will return null
	 */
	@Nullable Bond getBond(@Nullable String bondName);

	Set<? extends Bond> getBonds();

	/**
	 * Freezes this manager (i.e. won't take further changes)<br><br>
	 * <p>
	 * Freezing a {@link BondManager} freezes all of its {@link Bond bonds}.
	 *
	 * @throws IllegalStateException if the manager is already frozen
	 */
	void freeze() throws IllegalStateException;

	/**
	 * Checks whether the manager is frozen
	 *
	 * @return true if the bond if frozen, false if not
	 */
	boolean isFrozen();

	/**
	 * Unsets all the bonds the manager has
	 *
	 * @throws IllegalStateException if the bond is frozen
	 */
	void unset() throws IllegalStateException;

}
