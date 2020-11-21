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
	 * Unregisters a team.<br/>
	 * If the team doesn't exist, it does nothing.
	 *
	 * @param name The team's name
	 */
	void unregisterTeam(String name);

	Scoreboard getScoreboard();

}
