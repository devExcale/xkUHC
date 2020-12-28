package ovh.excale.mc.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.UHC;

import java.util.*;

public class ChallengerManager {

	private final Map<UUID, Challenger> challengerMap;
	private final ChallengerListener listener;
	private final Game game;

	public ChallengerManager(Game game) {
		challengerMap = Collections.synchronizedMap(new HashMap<>());
		listener = new ChallengerListener();
		this.game = game;
	}

	public @NotNull Challenger register(Player player) {

		Challenger challenger = new Challenger(player);
		challengerMap.put(challenger.getUniqueId(), challenger);

		return challenger;
	}

	public @Nullable Challenger get(UUID uniqueId) {
		return challengerMap.get(uniqueId);
	}

	public void remove(Player player) {
		Challenger challenger = challengerMap.remove(player.getUniqueId());

		if(challenger != null)
			challenger.setTeam(null);
	}

	public Set<Challenger> getAll() {
		return new HashSet<>(challengerMap.values());
	}

	public void reset() {

		//noinspection ConstantConditions
		Scoreboard mainScoreboard = Bukkit.getScoreboardManager()
				.getMainScoreboard();

		for(Challenger challenger : challengerMap.values())
			challenger.vanilla()
					.setScoreboard(mainScoreboard);
		challengerMap.clear();

	}

	public void listenChanges() {
		Bukkit.getPluginManager()
				.registerEvents(listener, UHC.plugin());
	}

	public void stopListener() {
		PlayerJoinEvent.getHandlerList()
				.unregister(listener);
		PlayerQuitEvent.getHandlerList()
				.unregister(listener);
	}

	private class ChallengerListener implements Listener {

		@EventHandler()
		private void onPlayerQuit(PlayerQuitEvent event) {

			Player player = event.getPlayer();
			Challenger challenger = challengerMap.get(player.getUniqueId());

			if(challenger != null) {

				challenger.setOnline(false);
				UUID challengerId = challenger.getUniqueId();

				if(challengerId.equals(game.getAdminId()) && game.getStatus() != Game.Status.RUNNING)
					game.reset();

			}

		}

		@EventHandler
		private void onPlayerJoin(PlayerJoinEvent event) {

			Player player = event.getPlayer();
			Challenger challenger = challengerMap.get(player.getUniqueId());

			if(challenger != null)
				challenger.updateReference(player);

		}

	}

}
