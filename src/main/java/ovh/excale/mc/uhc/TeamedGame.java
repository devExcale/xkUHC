package ovh.excale.mc.uhc;

import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.uhc.exceptions.GamePrepareException;

import java.util.Set;

public interface TeamedGame extends Game {

	Set<Team> getTeams();

	@Nullable Team getTeam(String name);

	Team createTeam(@NotNull String name) throws GamePrepareException;

	Scoreboard getScoreboard();

}
