package ovh.excale.mc.uhc;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import ovh.excale.mc.UHC;

import java.util.*;
import java.util.stream.Collectors;

public class Team {

	private static final Map<String, Team> teamMap = Collections.synchronizedMap(new HashMap<>());

	private final String name;
	private final Set<Challenger> members;
	private final org.bukkit.scoreboard.Team vanillaTeam;

	public static Team of(String name) {
		Team team = teamMap.get(name);
		if(team == null)
			teamMap.put(name, team = new Team(name));
		return team;
	}

	public static Set<Team> getAll() {
		return new HashSet<>(teamMap.values());
	}

	public static void clear() {
		for(Team team : teamMap.values())
			team.disband();
		teamMap.clear();

		Scoreboard scoreboard = UHC.scoreboard();
		for(org.bukkit.scoreboard.Team team : scoreboard.getTeams()) {
			team.unregister();
		}
	}

	private Team(String name) {
		this.members = Collections.synchronizedSet(new HashSet<>());
		this.name = name;

		vanillaTeam = UHC.scoreboard()
				.registerNewTeam(name);
		vanillaTeam.setPrefix("[" + name + "]");
		vanillaTeam.setDisplayName(name);
		vanillaTeam.setCanSeeFriendlyInvisibles(true);
		vanillaTeam.setAllowFriendlyFire(false);
	}

	public String getName() {
		return name;
	}

	public Set<Player> players() {
		return members.stream()
				.map(Challenger::vanilla)
				.collect(Collectors.toCollection(HashSet::new));
	}

	public Set<Challenger> challengers() {
		return new HashSet<>(members);
	}

	public int size() {
		return members.size();
	}

	public boolean add(Challenger challenger) {
		boolean b = challenger.getTeam() == null;
		if(b) {
			vanillaTeam.addEntry(challenger.vanilla()
					.getName());
			members.add(challenger);
			challenger.setTeam(this);
		}
		return b;
	}

	public boolean add(Player player) {
		Challenger challenger = Challenger.of(player);
		boolean b = challenger.getTeam() == null;

		if(b) {
			vanillaTeam.addEntry(player.getName());
			members.add(challenger);
			challenger.setTeam(this);
		}

		return b;
	}

	public boolean remove(Challenger challenger) {
		boolean b = challenger.getTeam() != null;

		if(b) {
			challenger.setTeam(null);
			vanillaTeam.removeEntry(challenger.vanilla()
					.getName());
			members.remove(challenger);
		}

		return b;
	}

	public boolean remove(Player player) {
		Challenger challenger = Challenger.of(player);
		boolean b = challenger.getTeam() != null;

		if(b) {
			challenger.setTeam(null);
			vanillaTeam.removeEntry(player.getName());
			members.remove(challenger);
		}

		return b;
	}

	public void disband() {
		vanillaTeam.unregister();
		teamMap.remove(name);

		for(Challenger challenger : members)
			challenger.setTeam(null);

		members.clear();
	}

	public void setColor(ChatColor color) {
		vanillaTeam.setColor(color);
	}

}
