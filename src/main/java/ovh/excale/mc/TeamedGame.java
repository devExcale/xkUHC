package ovh.excale.mc;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.exceptions.GamePrepareException;

import java.util.Set;

public interface TeamedGame extends Game {

	Set<Team> getTeams();

	@Nullable Team getTeam(String name);

	Team createTeam(@NotNull String name) throws GamePrepareException;

	/**
	 * Unregisters a team.
	 *
	 * @param name Team's name
	 * @return A set which containts the members of that team,
	 * or an empty set if the team doesn't exist.
	 */
	void unregisterTeam(String name);

	Scoreboard getScoreboard();

}
