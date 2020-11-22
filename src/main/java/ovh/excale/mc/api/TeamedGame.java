package ovh.excale.mc.api;

import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.UhcTeam;
import ovh.excale.mc.exceptions.GamePrepareException;

import java.util.Set;

public interface TeamedGame extends Game {

	@NotNull Scoreboard getScoreboard();

	Set<Team> getTeams();

	@Nullable Team getTeam(String name);

	Team createTeam(@NotNull String name) throws IllegalStateException;

	/**
	 * Unregisters a team.<br/>
	 * If the team doesn't exist, it does nothing.
	 *
	 * @param name The team's name
	 */
	boolean unregisterTeam(String name) throws IllegalStateException;

}
