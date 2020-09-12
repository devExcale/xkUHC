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
	private final Set<Challenger> playersAlive;
	private final Set<Challenger> playersDead;
	private final org.bukkit.scoreboard.Team vanillaTeam;
	private ChatColor color;

	public Team(@NotNull String name, @NotNull Scoreboard scoreboard) {
		playersAlive = Collections.synchronizedSet(new HashSet<>());
		playersDead = Collections.synchronizedSet(new HashSet<>());
		this.name = Objects.requireNonNull(name);

		vanillaTeam = scoreboard.registerNewTeam(name);
		vanillaTeam.setCanSeeFriendlyInvisibles(true);
		vanillaTeam.setAllowFriendlyFire(false);
	}

	public String getName() {
		return name;
	}

	public Set<Player> players() {
		return playersAlive.stream()
				.map(Challenger::vanilla)
				.collect(Collectors.toCollection(HashSet::new));
	}

	public Set<Challenger> challengers() {
		return Stream.concat(playersAlive.stream(), playersDead.stream())
				.collect(Collectors.toCollection(HashSet::new));
	}

	public boolean add(Player player) {
		Challenger challenger = Challenger.of(player);
		boolean b = challenger.getTeam() == null;

		if(b) {
			vanillaTeam.addEntry(player.getName());
			challenger.alive();
			playersAlive.add(challenger);
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
			playersAlive.remove(challenger);
		}

		return b;
	}

	public boolean remove(Player player) {
		Challenger challenger = Challenger.of(player);
		boolean b = challenger.getTeam() != null;

		if(b) {
			challenger.setTeam(null);
			vanillaTeam.removeEntry(player.getName());
			playersAlive.remove(challenger);
		}

		return b;
	}

	public void unregister() {
		vanillaTeam.unregister();

		for(Challenger challenger : playersAlive)
			challenger.setTeam(null);
		for(Challenger challenger : playersDead)
			challenger.setTeam(null);

		playersAlive.clear();
		playersDead.clear();
	}

	public ChatColor getColor() {
		return color;
	}

	public void setColor(@NotNull ChatColor color) {
		this.color = Objects.requireNonNull(color);
		vanillaTeam.setColor(color);
	}

}
