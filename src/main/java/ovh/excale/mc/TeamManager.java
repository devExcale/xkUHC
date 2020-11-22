package ovh.excale.mc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.api.Team;
import ovh.excale.mc.api.TeamedGame;

import java.util.*;

public class TeamManager {

	private final Map<String, Team> teams;
	private final TeamedGame game;

	// TODO: SET PLAYER SCOREBOARD ON TEAM ADD
	public TeamManager(TeamedGame game) {
		teams = Collections.synchronizedMap(new HashMap<>());
		this.game = game;
	}

	public @NotNull Team registerNewTeam(@NotNull String name) {
		unregisterTeam(name);

		Team team = new UhcTeam(name, game);
		teams.put(name, team);

		return team;
	}

	public boolean unregisterTeam(@NotNull String name) throws IllegalStateException {
		Team team = teams.remove(name);
		boolean b = team != null;

		if(b)
			team.unregister();

		return b;
	}

	public @Nullable Team getTeam(String name) {
		return teams.get(name);
	}

	public @NotNull Set<Team> getTeams() {
		return new HashSet<>(teams.values());
	}

	// TODO: IMPLEMENT METHOD
	public void unregisterAll() {

		for(Team team : teams.values())
			team.unregister();
		teams.clear();

	}

}
