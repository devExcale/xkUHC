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
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import ovh.excale.mc.uhc.Game;

import java.util.*;
import java.util.stream.Stream;

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
			throw new IllegalArgumentException("This player is already registered");

		gamer = new Gamer(player);
		gamers.put(gamer.getUniqueId(), gamer);

		return gamer;
	}

	// TODO: GAME STATUS
	public void unregister(Gamer gamer) throws IllegalStateException {

		Player player = gamer.getPlayer();
		bondRemoveGamer(gamer);

		gamers.remove(gamer.getUniqueId());
		//noinspection ConstantConditions
		player.setScoreboard(Bukkit.getScoreboardManager()
				.getMainScoreboard());

	}

	public Stream<Gamer> getGamers() {
		return gamers.values()
				.stream();
	}

	public Bond getBond(String name) {
		return bonds.get(name);
	}

	public Stream<Bond> getBonds() {
		return bonds.values()
				.stream();
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
		gamers.values()
				.stream()
				.map(Gamer::getScoreboard)
				.map(scoreboard -> scoreboard.getTeam(name))
				.filter(Objects::nonNull)
				.forEach(Team::unregister);

	}

	// TODO: GAME STATUS
	public void bondAddGamer(Bond bond, Gamer gamer) throws IllegalArgumentException, IllegalStateException {

		if(gamer.hasBond())
			throw new IllegalArgumentException("This player already has a bond");

		// TODO: EVENT
		bond.getInternalGamersMap()
				.put(gamer.getUniqueId(), gamer);

		gamer.setBond(bond);
		gamers.values()
				.stream()
				.map(Gamer::getScoreboard)
				.map(scoreboard -> scoreboard.getTeam(bond.getName()))
				.filter(Objects::nonNull)
				.forEach(team -> team.addEntry(gamer.getPlayer()
						.getName()));

	}

	// TODO: GAME STATUS
	public void bondRemoveGamer(Gamer gamer) throws IllegalArgumentException, IllegalStateException {

		if(!gamer.hasBond())
			throw new IllegalArgumentException("This player doesn't have a bond");

		// TODO: EVENT
		Bond bond = gamer.getBond();
		//noinspection ConstantConditions
		bond.getInternalGamersMap()
				.remove(gamer.getUniqueId());

		gamer.setBond(null);
		gamers.values()
				.stream()
				.map(Gamer::getScoreboard)
				.map(scoreboard -> scoreboard.getTeam(bond.getName()))
				.filter(Objects::nonNull)
				.forEach(team -> team.removeEntry(gamer.getPlayer()
						.getName()));

	}

	public void setColor(Bond bond, ChatColor color) throws IllegalArgumentException {

		if(color.isFormat())
			throw new IllegalArgumentException("Color ain't a color");

		bond.setColor(color);
		gamers.values()
				.stream()
				.map(Gamer::getScoreboard)
				.map(scoreboard -> scoreboard.getTeam(bond.getName()))
				.filter(Objects::nonNull)
				.forEach(team -> team.setColor(color));

	}

	public void setFriendlyFire(Bond bond, boolean friendlyFire) {

		bond.setFriendlyFire(friendlyFire);
		gamers.values()
				.stream()
				.map(Gamer::getScoreboard)
				.map(scoreboard -> scoreboard.getTeam(bond.getName()))
				.filter(Objects::nonNull)
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
		void onPlayerQuit(PlayerQuitEvent event) {

			Player player = event.getPlayer();
			Gamer gamer = gamers.get(player.getUniqueId());

			// TODO: RAISE EVENT

		}

		@EventHandler
		void onPlayerJoin(PlayerJoinEvent event) {

			Player player = event.getPlayer();
			Gamer gamer = gamers.get(player.getUniqueId());

			if(gamer != null) {
				gamer.updateReference(player);

				// TODO: RAISE EVENT

			}
		}

		@EventHandler
		void onPlayerDeath(PlayerDeathEvent event) {

			Player player = event.getEntity();
			Gamer gamer = gamers.get(player.getUniqueId());

			if(gamer != null) {

				// TODO: RAISE EVENT

			}

		}

	}

}
