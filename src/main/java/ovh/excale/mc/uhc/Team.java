package ovh.excale.mc.uhc;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Team {

	private final String name;
	private final Set<Challenger> players;
	private final org.bukkit.scoreboard.Team vanillaTeam;
	private ChatColor color;
	private boolean eliminated;

	public Team(@NotNull String name, @NotNull Scoreboard scoreboard) {
		players = Collections.synchronizedSet(new HashSet<>());
		this.name = Objects.requireNonNull(name);
		eliminated = false;

		vanillaTeam = scoreboard.registerNewTeam(name);
		vanillaTeam.setCanSeeFriendlyInvisibles(true);
		vanillaTeam.setAllowFriendlyFire(false);
	}

	public String getName() {
		return name;
	}

	public Set<Player> players() {
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
			vanillaTeam.addEntry(player.getName());
			players.add(challenger);
			challenger.setAlive(true);
			challenger.setTeam(this);
		}

		return b;
	}

	public boolean remove(Challenger challenger) {
		boolean b = equals(challenger.getTeam());

		if(b) {
			challenger.setTeam(null);
			vanillaTeam.removeEntry(challenger.vanilla()
					.getName());
			players.remove(challenger);
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
