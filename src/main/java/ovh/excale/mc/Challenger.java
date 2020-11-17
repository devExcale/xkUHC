package ovh.excale.mc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class Challenger {

	private static final Map<UUID, Challenger> challengerMap = Collections.synchronizedMap(new HashMap<>());

	public static void resetMapping() {

		//noinspection ConstantConditions
		Scoreboard mainScoreboard = Bukkit.getScoreboardManager()
				.getMainScoreboard();

		for(Challenger challenger : challengerMap.values()) {
			Player player = challenger.vanilla();
			player.setScoreboard(mainScoreboard);
		}
	}

	private final UUID uuid;
	private Player player;
	private Team team;
	private boolean alive;

	private Challenger(Player player) {
		uuid = player.getUniqueId();
		this.player = player;
		alive = true;
		team = null;
	}

	public static @NotNull Challenger of(Player player) {
		Challenger challenger = challengerMap.get(player.getUniqueId());
		if(challenger == null)
			challengerMap.put(player.getUniqueId(), challenger = new Challenger(player));
		else
			challenger.player = player;

		return challenger;
	}

	public static @Nullable Challenger get(Player player) {
		return challengerMap.get(player.getUniqueId());
	}

	public static Set<Challenger> teamUnbounds() {
		return challengerMap.values()
				.stream()
				.filter(challenger -> challenger.team == null)
				.collect(Collectors.toCollection(HashSet::new));
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public boolean isAlive() {
		return alive;
	}

	public boolean isDead() {
		return !alive;
	}

	public @NotNull Player vanilla() {
		return player;
	}

	public @NotNull UUID getUuid() {
		return uuid;
	}

	public void setTeam(@Nullable Team team) {
		this.team = team;
	}

	public @Nullable Team getTeam() {
		return team;
	}

	public boolean hasTeam() {
		return team != null;
	}

	public boolean is(Player player) {
		return player.getUniqueId()
				.equals(uuid);
	}

}
