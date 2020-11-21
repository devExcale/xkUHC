package ovh.excale.mc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ChallengerManager {

	private final Map<UUID, Challenger> challengerMap = Collections.synchronizedMap(new HashMap<>());

	public @NotNull Challenger wrap(Player player) {
		UUID uuid = player.getUniqueId();
		Challenger challenger = challengerMap.get(uuid);

		if(challenger == null)
			challengerMap.put(uuid, challenger = new Challenger(player));

		return challenger;
	}

	public void remove(Player player) {
		Challenger challenger = challengerMap.remove(player.getUniqueId());

		if(challenger != null)
			challenger.setTeam(null);
	}

	public void reset() {

		//noinspection ConstantConditions
		Scoreboard mainScoreboard = Bukkit.getScoreboardManager()
				.getMainScoreboard();

		for(Challenger challenger : challengerMap.values())
			challenger.vanilla()
					.setScoreboard(mainScoreboard);

	}

	public Set<Challenger> teamUnbounds() {
		return challengerMap.values()
				.stream()
				.filter(Challenger::hasTeam)
				.collect(Collectors.toCollection(HashSet::new));
	}

}
