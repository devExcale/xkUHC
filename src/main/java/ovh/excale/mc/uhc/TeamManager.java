package ovh.excale.mc.uhc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class TeamManager {

	private final Map<String, Team> teams;
	private final Scoreboard scoreboard;

	// TODO: SET PLAYER SCOREBOARD ON TEAM ADD
	public TeamManager(Scoreboard scoreboard) {
		teams = Collections.synchronizedMap(new HashMap<>());
		this.scoreboard = scoreboard;
	}

	public @NotNull Team registerNewTeam(@NotNull String name) {
		Team old = teams.get(name);

		if(old != null)
			old.unregister();

		Team team = new Team(name, scoreboard);
		teams.put(name, team);

		return team;
	}

	public @Nullable Team getTeam(String name) {
		return teams.get(name);
	}

	public Set<String> getTeamsList() {
		return teams.values()
				.stream()
				.map(Team::getName)
				.collect(Collectors.toCollection(HashSet::new));
	}

	public Set<Team> getTeams() {
		return new HashSet<>(teams.values());
	}

	public void purge() {
		for(String entry : scoreboard.getEntries()) {
			Player player = Bukkit.getPlayer(entry);

			if(player != null)
				//noinspection ConstantConditions
				player.setScoreboard(Bukkit.getScoreboardManager()
						.getMainScoreboard());
		}

		for(org.bukkit.scoreboard.Team team : scoreboard.getTeams()) {
			team.unregister();
		}

		for(DisplaySlot slot : DisplaySlot.values())
			scoreboard.clearSlot(slot);
	}

}
