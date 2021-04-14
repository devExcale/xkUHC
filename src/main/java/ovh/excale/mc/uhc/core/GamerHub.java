package ovh.excale.mc.uhc.core;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import ovh.excale.mc.uhc.Game;

import java.util.*;

public class GamerHub {

	private final Map<UUID, Gamer> gamers;
	private final Map<String, Bond> bonds;
	private final Game game;

	public GamerHub(Game game) {
		gamers = Collections.synchronizedMap(new HashMap<>());
		bonds = Collections.synchronizedMap(new HashMap<>());
		this.game = game;
	}

	// TODO: GAME STATUS
	public Gamer register(Player player) throws IllegalArgumentException, IllegalStateException {

		Gamer gamer = gamers.get(player.getUniqueId());
		if(gamer != null)
			throw new IllegalArgumentException("This player is already a gamer");

		gamer = new Gamer(player);
		gamers.put(gamer.getUniqueId(), gamer);

		Scoreboard scoreboard = gamer.getScoreboard();
		for(Bond bond : bonds.values()) {

			Team team = scoreboard.registerNewTeam(bond.getName());
			team.setColor(bond.getColor());
			team.setAllowFriendlyFire(bond.isFriendlyFireEnabled());

			for(Gamer bondGamer : bond.getGamers())
				team.addEntry(bondGamer.getPlayer()
						.getName());

		}

		game.getScoreboardProcessor()
				.track(gamer.getScoreboardPrinter());

		return gamer;
	}

	// TODO: GAME STATUS
	public void unregister(Gamer gamer) throws IllegalStateException {

		Player player = gamer.getPlayer();
		bondRemoveGamer(gamer);

		gamers.remove(player.getUniqueId());
		game.getScoreboardProcessor()
				.untrack(gamer.getScoreboardPrinter());

		//noinspection ConstantConditions
		player.setScoreboard(Bukkit.getScoreboardManager()
				.getMainScoreboard());

	}

	public Gamer getGamer(UUID uniqueId) {
		return gamers.get(uniqueId);
	}

	public Set<Gamer> getGamers() {
		return new HashSet<>(gamers.values());
	}

	public Bond getBond(String name) {
		return bonds.get(name);
	}

	public Set<Bond> getBonds() {
		return new HashSet<>(bonds.values());
	}

	/**
	 * @param name The name of the bond
	 * @return The new bond
	 * @throws IllegalArgumentException if a bond with the specified name already exists
	 * @throws IllegalStateException    if the game is running
	 */
	// TODO: NAME VALIDATION, GAME STATUS
	public Bond createBond(String name) throws IllegalStateException, IllegalArgumentException {

		Bond bond = bonds.get(name);
		if(bond != null)
			throw new IllegalArgumentException("A bond named '" + name + "' already exists");

		System.out.println(gamers.size());

		// TODO: EVENT
		bond = new Bond(name, game);
		gamers.forEach((uuid, gamer) -> gamer.getScoreboard()
				.registerNewTeam(name));
		bonds.put(name, bond);

		return bond;
	}

	// TODO: GAME STATUS
	public void removeBond(String name) throws IllegalArgumentException, IllegalStateException {

		Bond bond = bonds.remove(name);
		if(bond == null)
			throw new IllegalArgumentException("There's no bond named '" + name + "'");

		// TODO: EVENT
		bond.getGamers()
				.forEach(gamer -> gamer.setBond(null));
		//noinspection ConstantConditions
		gamers.values()
				.stream()
				.map(Gamer::getScoreboard)
				.map(scoreboard -> scoreboard.getTeam(name))
				.forEach(Team::unregister);

	}

	// TODO: GAME STATUS
	// TODO: RENAME TO BOUND_GAMER
	public void bondAddGamer(Bond bond, Gamer gamer) throws IllegalArgumentException, IllegalStateException {
		// TODO: EVENT

		if(gamer.hasBond())
			throw new IllegalArgumentException("This player already has a bond");

		System.out.println(gamers.size());

		//noinspection ConstantConditions
		gamers.values()
				.stream()
				.map(Gamer::getScoreboard)
				.map(scoreboard -> scoreboard.getTeam(bond.getName()))
				.forEach(team -> team.addEntry(gamer.getPlayer()
						.getName()));

		gamer.setBond(bond);
		bond.getInternalGamersSet()
				.add(gamer);

	}

	// TODO: GAME STATUS
	// TODO: RENAME TO UNBOUND_GAMER
	public void bondRemoveGamer(Gamer gamer) throws IllegalArgumentException, IllegalStateException {

		if(!gamer.hasBond())
			throw new IllegalArgumentException("This player doesn't have a bond");

		// TODO: EVENT
		Bond bond = gamer.getBond();
		//noinspection ConstantConditions
		bond.getInternalGamersSet()
				.remove(gamer);

		gamer.setBond(null);
		//noinspection ConstantConditions
		gamers.values()
				.stream()
				.map(Gamer::getScoreboard)
				.map(scoreboard -> scoreboard.getTeam(bond.getName()))
				.forEach(team -> team.removeEntry(gamer.getPlayer()
						.getName()));

	}

	public void setBondColor(Bond bond, ChatColor color) throws IllegalArgumentException {

		if(color.isFormat())
			throw new IllegalArgumentException("Color ain't a color");

		bond.setColor(color);
		//noinspection ConstantConditions
		gamers.values()
				.stream()
				.map(Gamer::getScoreboard)
				.map(scoreboard -> scoreboard.getTeam(bond.getName()))
				.forEach(team -> team.setColor(color));

	}

	public void setEnableFriendlyFire(Bond bond, boolean friendlyFire) {

		bond.setFriendlyFire(friendlyFire);
		//noinspection ConstantConditions
		gamers.values()
				.stream()
				.map(Gamer::getScoreboard)
				.map(scoreboard -> scoreboard.getTeam(bond.getName()))
				.forEach(team -> team.setAllowFriendlyFire(friendlyFire));

	}

	public void broadcast(@NotNull String message) {
		gamers.forEach((uuid, gamer) -> gamer.getPlayer()
				.sendMessage(message));
	}

	public void broadcast(@NotNull BaseComponent message) {
		gamers.forEach((uuid, gamer) -> gamer.getPlayer()
				.spigot()
				.sendMessage(message));
	}

	public void dispose() {

	}

	public class GamerHandler implements Listener {

		@EventHandler
		private void onPlayerQuit(PlayerQuitEvent event) {

			Player player = event.getPlayer();
			Gamer gamer = gamers.get(player.getUniqueId());

			// TODO: RAISE EVENT

		}

		@EventHandler
		private void onPlayerJoin(PlayerJoinEvent event) {

			Player player = event.getPlayer();
			Gamer gamer = gamers.get(player.getUniqueId());

			if(gamer != null) {
				gamer.updateReference(player);

				// TODO: RAISE EVENT

			}
		}

		@EventHandler
		private void onPlayerDeath(PlayerDeathEvent event) {

			Player player = event.getEntity();
			Gamer gamer = gamers.get(player.getUniqueId());

			if(gamer != null) {

				// TODO: RAISE EVENT

			}

		}

	}

}
