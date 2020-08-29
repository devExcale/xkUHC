package ovh.excale.mc.uhc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.UHC;
import ovh.excale.mc.uhc.events.ChallengerDisconnectEvent;
import ovh.excale.mc.uhc.events.ChallengerJoinEvent;

import java.util.*;
import java.util.stream.Collectors;

public class Challenger {

	private static final Map<UUID, Challenger> challengerMap = Collections.synchronizedMap(new HashMap<>());

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

	public static boolean is(Player player) {
		return challengerMap.containsKey(player.getUniqueId());
	}

	public static Set<Challenger> teamUnbound() {
		return challengerMap.values()
				.stream()
				.filter(challenger -> challenger.team == null)
				.collect(Collectors.toCollection(HashSet::new));
	}

	public void alive() {
		alive = true;
	}

	public void die() {
		alive = false;
	}

	public @NotNull Player vanilla() {
		return player;
	}

	public @NotNull UUID getUuid() {
		return uuid;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public Team getTeam() {
		return team;
	}

	public static class DisconnectListener implements Listener {

		private static final DisconnectListener instance = new DisconnectListener();
		private static final Set<UUID> disconnectPool = Collections.synchronizedSet(new HashSet<>());

		private boolean listening;

		private DisconnectListener() {
			listening = false;
		}

		public static DisconnectListener getInstance() {
			return instance;
		}

		public void start() {
			if(!listening) {
				listening = true;

				Bukkit.getPluginManager()
						.registerEvents(this, UHC.plugin());
			}
		}

		public void stop() {
			if(listening) {
				listening = false;

				PlayerQuitEvent.getHandlerList()
						.unregister(instance);
				PlayerJoinEvent.getHandlerList()
						.unregister(instance);
			}
		}

		public boolean isListening() {
			return listening;
		}

		@EventHandler
		private void onPlayerDisconnect(PlayerQuitEvent event) {
			Player player = event.getPlayer();
			Challenger challenger = Challenger.get(player);

			if(challenger != null) {
				disconnectPool.add(player.getUniqueId());
				Bukkit.getPluginManager()
						.callEvent(new ChallengerDisconnectEvent(challenger));
			}
		}

		@EventHandler
		private void onPlayerJoin(PlayerJoinEvent event) {
			Player player = event.getPlayer();
			Challenger challenger = challengerMap.get(player.getUniqueId());

			if(challenger != null)
				Bukkit.getPluginManager()
						.callEvent(new ChallengerJoinEvent(Challenger.of(player)));
		}

	}

}
