package ovh.excale.mc;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Team {

	private final String name;
	private final Set<Challenger> players;
	private final org.bukkit.scoreboard.Team vanillaTeam;
	private final Scoreboard scoreboard;
	private ChatColor color;
	private boolean eliminated;

	protected Team(@NotNull String name, @NotNull Scoreboard scoreboard, boolean friendlyFire) throws IllegalArgumentException {
		players = Collections.synchronizedSet(new HashSet<>());
		this.name = Objects.requireNonNull(name);
		this.scoreboard = scoreboard;
		color = ChatColor.WHITE;
		eliminated = false;

		vanillaTeam = scoreboard.registerNewTeam(name);
		vanillaTeam.setCanSeeFriendlyInvisibles(true);
		vanillaTeam.setAllowFriendlyFire(friendlyFire);
	}

	public String getName() {
		return name;
	}

	public Set<Player> members() {
		return players.stream()
				.map(Challenger::vanilla)
				.collect(Collectors.toCollection(HashSet::new));
	}

	public Set<Challenger> challengers() {
		return new HashSet<>(players);
	}

	public boolean add(Player player) {
		Challenger challenger = Challenger.of(player);
		boolean b = challenger.getTeam() == null;

		if(b) {
			challenger.setAlive(true);
			challenger.setTeam(this);
			players.add(challenger);
			vanillaTeam.addEntry(player.getName());
		}

		return b;
	}

	public boolean remove(Player player) {
		Challenger challenger = Challenger.of(player);
		boolean b = equals(challenger.getTeam());

		if(b) {
			challenger.setTeam(null);
			players.remove(challenger);
			vanillaTeam.removeEntry(player.getName());
		}

		return b;
	}

	public boolean isEliminated() {
		if(!eliminated)
			eliminated = players.stream()
					.noneMatch(Challenger::isAlive);

		return eliminated;
	}

	public boolean isAlive() {
		if(!eliminated)
			eliminated = players.stream()
					.noneMatch(Challenger::isAlive);

		return !eliminated;
	}

	public void unregister() {
		vanillaTeam.unregister();

		for(Challenger challenger : players)
			challenger.setTeam(null);

		players.clear();
	}

	public ChatColor getColor() {
		return color;
	}

	public void setColor(@NotNull ChatColor color) {
		this.color = Objects.requireNonNull(color);
		vanillaTeam.setColor(color);
	}

}
