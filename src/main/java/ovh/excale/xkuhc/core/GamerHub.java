package ovh.excale.xkuhc.core;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.comms.MessageBundles;
import ovh.excale.xkuhc.events.bond.BondCreateAsyncEvent;
import ovh.excale.xkuhc.events.bond.BondDeleteAsyncEvent;
import ovh.excale.xkuhc.events.bond.BondSetColorAsyncEvent;
import ovh.excale.xkuhc.events.gamer.*;
import ovh.excale.xkuhc.xkUHC;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.WHITE;
import static ovh.excale.xkuhc.core.GamePhase.RUNNING;

public class GamerHub {

	public static final Pattern BOND_NAME_REGEX = Pattern.compile("([#@\\[a-zA-Z0-9~\\]_+-.]){3,}");

	private final Map<UUID, Gamer> gamers;
	private final Map<String, Bond> bonds;
	private final EventRaiser eventRaiser;
	private final Game game;

	private final Logger log;
	private final MessageBundles msg;

	public GamerHub(Game game) {
		this.game = game;
		gamers = Collections.synchronizedMap(new HashMap<>());
		bonds = Collections.synchronizedMap(new HashMap<>());
		eventRaiser = new EventRaiser();

		xkUHC instance = xkUHC.instance();
		log = instance.getLogger();
		msg = instance.getMessages();

	}

	public Gamer register(Player player) throws IllegalArgumentException, IllegalStateException {

		statusCheck();

		if(player == null)
			return null;

		Gamer gamer = gamers.get(player.getUniqueId());
		if(gamer != null)
			throw new IllegalArgumentException("This player is already a gamer");

		gamer = new Gamer(game, player);
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

		xkUHC.call(new GamerRegisterEvent(gamer));

		return gamer;
	}

	public void unregister(Gamer gamer) throws IllegalStateException {

		statusCheck();

		if(gamer == null)
			return;

		Player player = gamer.getPlayer();
		unboundGamer(gamer);

		gamers.remove(player.getUniqueId());
		game.getScoreboardProcessor()
				.untrack(gamer.getScoreboardPrinter());

		//noinspection ConstantConditions
		player.setScoreboard(Bukkit.getScoreboardManager()
				.getMainScoreboard());

		xkUHC.call(new GamerUnregisterEvent(gamer));

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
	 * Get all {@link ChatColor}s bounded to all existing bonds except of the white one.
	 *
	 * @return a {@link Set}<{@link ChatColor}> that contains all the color binded to the bonds, expect for the WHITE
	 */
	public Set<ChatColor> getBondColors() {

		return bonds.values()
				.stream()
				.map(Bond::getColor)
				.filter(color -> !color.equals(WHITE))
				.collect(Collectors.toSet());

	}

	/**
	 * @param name The name of the bond
	 * @return The new bond
	 * @throws IllegalArgumentException if a bond with the specified name already exists
	 * @throws IllegalStateException    if the game is running
	 */
	public Bond createBond(String name) throws IllegalStateException, IllegalArgumentException {

		statusCheck();

		if(name == null || !BOND_NAME_REGEX.matcher(name)
				.matches())
			throw new IllegalArgumentException("Illegal bond name: " + name);

		Bond bond = bonds.get(name);
		if(bond != null)
			throw new IllegalArgumentException("A bond named '" + name + "' already exists");

		bond = new Bond(name, game);
		gamers.forEach((uuid, gamer) -> gamer.getScoreboard()
				.registerNewTeam(name));
		bonds.put(name, bond);

		xkUHC.callAsync(new BondCreateAsyncEvent(bond));

		return bond;
	}

	public void removeBond(String name) throws IllegalArgumentException, IllegalStateException {

		Bond bond = bonds.remove(name);
		if(bond == null)
			throw new IllegalArgumentException("There's no bond named '" + name + "'");

		breakBond(bond);

	}

	public void breakBond(Bond bond) throws IllegalStateException {

		statusCheck();

		bond.getGamers()
				.forEach(gamer -> gamer.setBond(null));
		//noinspection ConstantConditions
		gamers.values()
				.stream()
				.map(Gamer::getScoreboard)
				.map(scoreboard -> scoreboard.getTeam(bond.getName()))
				.forEach(Team::unregister);

		bonds.remove(bond.getName());

		xkUHC.callAsync(new BondDeleteAsyncEvent(bond));

	}

	public void boundGamer(Bond bond, Gamer gamer) throws IllegalArgumentException, IllegalStateException {

		statusCheck();

		if(gamer.hasBond())
			throw new IllegalArgumentException("This player already has a bond");

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

	public void unboundGamer(Gamer gamer) throws IllegalArgumentException, IllegalStateException {

		statusCheck();

		if(!gamer.hasBond())
			throw new IllegalArgumentException("This player doesn't have a bond");

		Bond bond = gamer.getBond();
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

	public void setBondColor(Bond bond, ChatColor color) throws IllegalArgumentException, IllegalStateException {

		statusCheck();

		if(color.isFormat())
			throw new IllegalArgumentException("Color ain't a color");

		bond.setColor(color);
		//noinspection ConstantConditions
		gamers.values()
				.stream()
				.map(Gamer::getScoreboard)
				.map(scoreboard -> scoreboard.getTeam(bond.getName()))
				.forEach(team -> team.setColor(color));

		xkUHC.callAsync(new BondSetColorAsyncEvent(bond));

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

	public void forEachPlayer(Consumer<? super Gamer> action) {
		gamers.values()
				.forEach(action);
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

	public void broadcastDead(@NotNull String message) {
		for(Gamer gamer : gamers.values())
			if(!gamer.isAlive())
				gamer.getPlayer()
						.sendMessage(message);
	}

	public void broadcastDead(@NotNull BaseComponent message) {
		for(Gamer gamer : gamers.values())
			if(!gamer.isAlive())
				gamer.getPlayer()
						.spigot()
						.sendMessage(message);
	}

	public void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		gamers.forEach((uuid, gamer) -> gamer.getPlayer()
				.sendTitle(title, subtitle, fadeIn, stay, fadeOut));
	}

	public void broadcastTitle(String title, String subtitle) {
		broadcastTitle(title, subtitle, -1, -1, -1);
	}

	public void broadcastSound(Sound sound, float volume, float pitch) {
		gamers.forEach((uuid, gamer) -> gamer.getPlayer()
				.playSound(gamer.getPlayer(), sound, volume, pitch));
	}

	public void unset() {

		for(Gamer gamer : Set.copyOf(gamers.values()))
			unregister(gamer);

		for(Bond bond : Set.copyOf(bonds.values()))
			breakBond(bond);

		gamers.clear();
		bonds.clear();

	}

	private void statusCheck() throws IllegalStateException {

		if(game.getPhase()
				.isRunning())
			throw new IllegalStateException("The game is not editable right now");

	}

	protected EventRaiser getEventRaiser() {
		return eventRaiser;
	}

	public class EventRaiser implements Listener, GameAccessory {

		private final Map<Class<? extends Event>, EventExecutor> executors;
		private final PluginManager pluginManager;
		private boolean enabled;

		public EventRaiser() {
			executors = new HashMap<>();
			pluginManager = Bukkit.getPluginManager();
			enabled = false;

			// JOIN EVENT EXECUTOR
			executors.put(PlayerJoinEvent.class, (listener, event) -> ((EventRaiser) listener).onPlayerJoin((PlayerJoinEvent) event));

			// QUIT EVENT EXECUTOR
			executors.put(PlayerQuitEvent.class, (listener, event) -> ((EventRaiser) listener).onPlayerQuit((PlayerQuitEvent) event));

			// DAMAGE EVENT EXECUTOR
			executors.put(EntityDamageEvent.class, (listener, event) -> ((EventRaiser) listener).onEntityDamage((EntityDamageEvent) event));

			// CHAT EVENT EXECUTOR
			executors.put(AsyncPlayerChatEvent.class, (listener, event) -> ((EventRaiser) listener).onPlayerChat((AsyncPlayerChatEvent) event));

		}

		public void enable() {

			if(!enabled) {

				executors.forEach(
						(eventClass, eventExecutor) -> pluginManager.registerEvent(eventClass, EventRaiser.this, EventPriority.HIGH, eventExecutor, xkUHC.instance(), true));
				enabled = true;

			}

		}

		public void disable() {

			if(enabled)
				for(Class<? extends Event> eventClass : executors.keySet())
					try {

						((HandlerList) eventClass.getDeclaredMethod("getHandlerList")
								.invoke(null)).unregister(EventRaiser.this);

					} catch(NoSuchMethodException e) {

						log.log(Level.SEVERE, "Event " + eventClass.getSimpleName() + " is missing static method getHandlerList()", e);

					} catch(InvocationTargetException | IllegalAccessException e) {

						log.log(Level.SEVERE, "Cannot access HandlerList in class " + eventClass.getSimpleName(), e);

					}

			enabled = false;

		}

		public boolean isEnabled() {
			return enabled;
		}

		@Override
		public void onPhaseChange(@NotNull GamePhase phase) {

			switch(phase) {

				case READY -> enable();

				case STOPPED -> disable();

			}

		}

		@EventHandler
		private void onPlayerQuit(PlayerQuitEvent event) {

			Player player = event.getPlayer();
			Gamer gamer = gamers.get(player.getUniqueId());

			if(gamer != null) {

				gamer.takeSnapshot();
				gamer.resetPlayer();
				pluginManager.callEvent(new GamerDisconnectEvent(gamer, event));

			}

		}

		@EventHandler
		private void onPlayerJoin(PlayerJoinEvent event) {

			Player player = event.getPlayer();
			Gamer gamer = gamers.get(player.getUniqueId());

			if(gamer != null) {

				gamer.applySnapshot(player);
				pluginManager.callEvent(new GamerReconnectEvent(gamer, event));

			}

		}

		@EventHandler
		private void onEntityDamage(EntityDamageEvent event) {

			if(game.getPhase() != RUNNING)
				return;

			Entity entity = event.getEntity();
			Gamer gamer = gamers.get(entity.getUniqueId());

			if(gamer == null)
				return;

			if(((Player) entity).getHealth() - event.getDamage() > 0)
				return;

			event.setCancelled(true);
			pluginManager.callEvent(new GamerDeathEvent(gamer, event, GamerHub.this));

		}

		@EventHandler
		private void onPlayerChat(AsyncPlayerChatEvent event) {

			Player player = event.getPlayer();
			Gamer gamer = gamers.get(player.getUniqueId());

			if(gamer != null && gamer.hasBond()) {

				event.setCancelled(true);

				Bond bond = gamer.getBond();

				String formattedMsg = msg.game(gamer.isAlive() ? "chat.bond" : "chat.dead")
						.gamer(gamer)
						.bond(bond)
						.custom("message", event.getMessage())
						.format();

				if(gamer.isAlive())
					bond.broadcast(formattedMsg);
				else
					GamerHub.this.broadcastDead(formattedMsg);

			}
		}

	}

}
