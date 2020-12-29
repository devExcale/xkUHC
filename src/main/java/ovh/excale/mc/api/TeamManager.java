package ovh.excale.mc.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.UhcTeam;

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
		// TODO: tf is this unregister? need to clean up this class
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

	public void validate() {
		for(Team team : teams.values()) {
			team.validate();
			if(team.getMembers()
					.size() == 0)
				unregisterTeam(team.getName());
		}
	}

	// TODO: IMPLEMENT METHOD
	public void unregisterAll() {

		for(Team team : teams.values())
			team.unregister();
		teams.clear();

	}

}
