package ovh.excale.xkuhc.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.comms.MessageBundles;
import ovh.excale.xkuhc.comms.MessageFormatter;
import ovh.excale.xkuhc.comms.ScoreboardProcessor;
import ovh.excale.xkuhc.configuration.BorderAction;
import ovh.excale.xkuhc.configuration.GameSettings;
import ovh.excale.xkuhc.discord.DiscordEndpoint;
import ovh.excale.xkuhc.eventhandlers.AsyncTeleportAnchor;
import ovh.excale.xkuhc.eventhandlers.BedInteractionHandler;
import ovh.excale.xkuhc.eventhandlers.GodModeHandler;
import ovh.excale.xkuhc.eventhandlers.MobRepellentHandler;
import ovh.excale.xkuhc.events.*;
import ovh.excale.xkuhc.world.PlayerSpreader;
import ovh.excale.xkuhc.world.WorldManager;
import ovh.excale.xkuhc.xkUHC;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.BOLD;
import static org.bukkit.ChatColor.RESET;
import static org.bukkit.GameMode.SPECTATOR;
import static org.bukkit.GameMode.SURVIVAL;
import static org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
import static org.bukkit.Sound.ENTITY_PLAYER_ATTACK_CRIT;
import static org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH;
import static ovh.excale.xkuhc.configuration.BorderAction.ActionType.MOVE;
import static ovh.excale.xkuhc.core.Game.Phase.*;

public class Game implements Listener {

	private final Logger log;

	private final GamerHub hub;
	private final Stopwatch stopwatch;
	private final ScoreboardProcessor scoreboardProcessor;
	private final MessageBundles msg;

	private final BedInteractionHandler bedHandler;
	private final MobRepellentHandler mobRepellent;
	private final GodModeHandler godMode;

	private BukkitTask runTask;
	private Phase phase;
	// TODO: SET STATUS
	// TODO: STATUS EVENTS

	private World world;
	private GameSettings settings;
	private Iterator<BorderAction> borderActions;
	private BorderAction currentAction;
	private boolean confirmStart;

	public Game() {
		hub = new GamerHub(this);
		stopwatch = new Stopwatch();
		scoreboardProcessor = new ScoreboardProcessor();

		bedHandler = new BedInteractionHandler();
		mobRepellent = new MobRepellentHandler(this);
		godMode = new GodModeHandler();

		xkUHC instance = xkUHC.instance();
		msg = instance.getMessages();
		log = instance.getLogger();

		runTask = null;
		phase = READY;

		world = null;
		settings = null;
		currentAction = null;
		confirmStart = false;

		PluginManager pluginManager = Bukkit.getPluginManager();

		// GAMER RECONNECT EVENT
		pluginManager.registerEvent(GamerReconnectEvent.class, this, EventPriority.HIGH, (listener, event) -> ((Game) listener).onGamerReconnect((GamerReconnectEvent) event),
				xkUHC.instance());
		// GAMER DISCONNECT EVENT
		pluginManager.registerEvent(GamerDisconnectEvent.class, this, EventPriority.HIGH, (listener, event) -> ((Game) listener).onGamerDisconnect((GamerDisconnectEvent) event),
				xkUHC.instance());
		// GAMER DEATH EVENT
		pluginManager.registerEvent(GamerDeathEvent.class, this, EventPriority.HIGH, (listener, event) -> ((Game) listener).onGamerDeath((GamerDeathEvent) event),
				xkUHC.instance());

		initScoreboardProcessor();

	}

	private void initScoreboardProcessor() {

		// SCOREBOARD
		// (14)
		// (13) > Bond:
		// (12) > Gamer:
		// (11) > Kills:
		// (10)
		// ( 9) > Border:
		// ( 8) > Phase:
		// ( 7) > Remaining:
		// ( 6)
		// ( 5) > Bonds:
		// ( 4) > Gamers:
		// ( 3)
		// ( 2) [ 1h:32m:03s ]
		// ( 1) [ 000, 00, 000 ]
		// ( 0)

		// TODO: USE MessageFormatter
		// BOND
		scoreboardProcessor.print(13, gamer -> {

			String s = "> " + BOLD + "Bond: " + RESET;
			Bond bond = gamer.getBond();

			if(bond != null)
				s += bond.getColor() + bond.getName() + RESET;
			else
				s += "UNBOUND";

			return s;
		});

		// GAMER
		scoreboardProcessor.print(12, gamer -> {

			String s = "> " + BOLD + "Gamer: " + RESET;
			Bond bond = gamer.getBond();
			Player player = gamer.getPlayer();

			if(bond != null)
				s += bond.getColor() + player.getName() + RESET;
			else
				s += player.getName();

			return s;
		});

		// KILLS
		scoreboardProcessor.print(11, gamer -> "> " + BOLD + "Kills: " + RESET + gamer.getKillCount());

		// BORDER
		scoreboardProcessor.print(9, gamer -> {

			int size;
			if(world != null) {
				WorldBorder border = world.getWorldBorder();
				size = (int) (border.getSize() / 2);
			} else
				size = 0;

			return "> " + BOLD + "Border: " + RESET + String.format("[%d, %d]", -size, size);
		});

		// STATUS
		scoreboardProcessor.print(8, gamer -> {

			String action = (currentAction == null)
					? "none"
					: currentAction.getType()
							.toString()
							.toLowerCase() + "ing";

			return "> " + BOLD + "Phase: " + RESET + action;
		});

		// REMAINING
		scoreboardProcessor.print(7, gamer -> {

			String s = "> " + BOLD + "Remaining:" + RESET + " N/A";
			if(currentAction != null)
				s = "> " + BOLD + "Remaining: " + RESET + (currentAction.getTime() - stopwatch.getLapDelta()) + "s";

			return s;
		});

		// BONDS
		scoreboardProcessor.print(5, gamer -> "> " + BOLD + "Bonds: " + RESET + (int) hub.getBonds()
				.stream()
				.filter(Bond::isAlive)
				.count());

		// GAMERS
		scoreboardProcessor.print(4, gamer -> "> " + BOLD + "Gamers: " + RESET + (int) hub.getGamers()
				.stream()
				.filter(Gamer::isAlive)
				.count());

		// TIME
		scoreboardProcessor.print(2, gamer -> {

			String str = "[ ";

			int s = stopwatch.getSeconds();
			int m = stopwatch.getMinutes();
			int h = m / 60;
			if(h > 0)
				str += (m / 60) + "h:";

			return String.format(str + "%2dm:%2ds ]", m % 60, s);
		});

		// POSITION
		scoreboardProcessor.print(1, gamer -> {

			Location loc = gamer.getPlayer()
					.getLocation();

			return String.format("[ %d, %d, %d ]", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		});

	}

	public Phase getPhase() {
		return phase;
	}

	public GamerHub getHub() {
		return hub;
	}

	public ScoreboardProcessor getScoreboardProcessor() {
		return scoreboardProcessor;
	}

	public boolean getConfirmStart() {
		return confirmStart;
	}

	public void setConfirmStart(boolean confirm) {
		confirmStart = confirm;
	}

	public void tryStart() throws IllegalStateException {

		settings = GameSettings.fromConfig();
		if(!settings.isLegal())
			throw new IllegalStateException(settings.getErrorMessage());

		Set<Gamer> removableGamers = hub.getGamers()
				.stream()
				.filter(gamer -> !gamer.isOnline())
				.collect(Collectors.toSet());

		for(Gamer gamer : removableGamers)
			hub.unregister(gamer);

		hub.getBonds()
				.stream()
				.filter(bond -> bond.size() == 0)
				.forEach(bond -> hub.removeBond(bond.getName()));

		if(hub.getBonds()
				.size() < 2)
			throw new IllegalStateException(msg.main("game.no_bonds"));

		DiscordEndpoint discord = DiscordEndpoint.getInstance();
		if(discord != null) {

			if(!discord.hasMainChannel())
				throw new IllegalStateException(msg.discord("error.no_main_ch"));

			Set<UUID> linked = discord.getLinkedPlayers();
			String unlinked = hub.getGamers()
					.stream()
					.map(Gamer::getPlayer)
					.filter(player -> !linked.contains(player.getUniqueId()))
					.map(Player::getDisplayName)
					.collect(Collectors.joining(", "));

			String message = new MessageFormatter().custom("unlinked", unlinked)
					.format(msg.discord("error.unlinked"));

			if(!unlinked.isEmpty() && !confirmStart)
				throw new IllegalStateException(message);

		}

		borderActions = settings.getBorderActionIterator();

		Bukkit.getScheduler()
				.runTaskAsynchronously(xkUHC.instance(), this::start);
	}

	private void start() {

		phase = STARTING;

		hub.broadcast(msg.game("game.loading"));

		world = new WorldManager(log).loadSpawn(false)
				.generateUntilClearCenter()
				.applyRules()
				.getWorld();

		//noinspection ConstantConditions
		WorldBorder border = world.getWorldBorder();
		int initialBorder = settings.getInitialBorderSize();

		Bukkit.getScheduler()
				.callSyncMethod(xkUHC.instance(), () -> {
					border.setSize(initialBorder);
					return null;
				});

		// broadcast world stats

		for(Gamer gamer : hub.getGamers())
			gamer.initForGame(true);

		hub.broadcast(msg.game("game.teleporting"));

		PlayerSpreader spreader = new PlayerSpreader(world, initialBorder - 80);
		AsyncTeleportAnchor tpAnchor = new AsyncTeleportAnchor();

		godMode.setIds(hub.getGamers()
				.stream()
				.map(Gamer::getPlayer)
				.map(Entity::getUniqueId)
				.collect(Collectors.toSet()));
		godMode.enable();

		Bukkit.getScheduler()
				.callSyncMethod(xkUHC.instance(), () -> {

					for(Bond bond : hub.getBonds()) {

						hub.setEnableFriendlyFire(bond, settings.isFriendlyFireEnabled());

						Player[] players = bond.getGamers()
								.stream()
								.map(Gamer::getPlayer)
								.toArray(Player[]::new);

						spreader.spread(players);
						tpAnchor.waitFor(players);

					}

					return null;
				});

		tpAnchor.await();

		try {
			Thread.sleep(10000);
		} catch(InterruptedException ignored) {
		}

		hub.broadcastTitle("3", "", 5, 15, 0);
		hub.broadcastSound(ENTITY_EXPERIENCE_ORB_PICKUP, 100, 0);

		try {
			Thread.sleep(1000);
		} catch(InterruptedException ignored) {
		}

		hub.broadcastTitle("2", "");
		hub.broadcastSound(ENTITY_EXPERIENCE_ORB_PICKUP, 100, 0);

		try {
			Thread.sleep(1000);
		} catch(InterruptedException ignored) {
		}

		hub.broadcastTitle("1", "");
		hub.broadcastSound(ENTITY_EXPERIENCE_ORB_PICKUP, 100, 0);

		try {
			Thread.sleep(1000);
		} catch(InterruptedException ignored) {
		}

		Bukkit.getScheduler()
				.callSyncMethod(xkUHC.instance(), () -> {

					for(Gamer gamer : hub.getGamers())
						gamer.removePotionEffects();

					return null;
				});

		// Call GameStartEvent on game start
		Bukkit.getPluginManager()
				.callEvent(new GameStartEvent(this));

		hub.broadcastTitle(msg.game("game.title"), msg.game("game.subtitle"), 10, 70, 20);
		hub.broadcastSound(ENTITY_EXPERIENCE_ORB_PICKUP, 100, 1);

		runTask = Bukkit.getScheduler()
				.runTaskAsynchronously(xkUHC.instance(), this::run);
		phase = RUNNING;

		godMode.enableTime(200);

		stopwatch.start();
		bedHandler.activate();
		mobRepellent.activate();

	}

	private void run() {

		stopwatch.lap();

		currentAction = borderActions.next();
		BorderAction.ActionType actionType = currentAction.getType();

		BukkitScheduler scheduler = Bukkit.getScheduler();

		if(actionType == MOVE)
			scheduler.callSyncMethod(xkUHC.instance(), () -> {

				world.getWorldBorder()
						.setSize(currentAction.getBorderSize(), currentAction.getTime());
				return null;

			});

		hub.broadcastSound(ENTITY_EXPERIENCE_ORB_PICKUP, 100, 0);
		hub.broadcast(new MessageFormatter().addColors()
				.formatFine(msg.game(actionType.getMessageKey())));

		runTask = scheduler.runTaskLaterAsynchronously(xkUHC.instance(), borderActions.hasNext() ? this::run : this::end, currentAction.getTime() * 20L);

	}

	/**
	 * Ends the game gracefully, user interactive.
	 */
	public void end() throws IllegalStateException {

		if(phase != RUNNING && phase != LETHAL)
			throw new IllegalStateException(msg.main("game.not_stoppable"));

		if(!runTask.isCancelled())
			runTask.cancel();

		phase = END;

		// TODO: other checks/messages (move win condition over here)

		godMode.enable();

		MessageFormatter formatter = new MessageFormatter().addColors();

		hub.broadcast(formatter.formatFine(msg.main("game.end_tp")));

		// TODO: call GameEndEvent

		runTask = Bukkit.getScheduler()
				.runTaskLater(xkUHC.instance(), this::stop, 800);

	}

	/**
	 * Stops abruptly the game, without user interaction.
	 * Needs to be called when the game needs to be stopped instantly.
	 * <br>
	 * (e.g. server stop, plugin deactivation)
	 *
	 * <br><br>
	 * <p>
	 * This method <b>must</b> be called sync!
	 * <br>
	 * This method will be called after {@link #end()}.
	 *
	 * @throws IllegalStateException when game isn't running
	 */
	public void stop() throws IllegalStateException {

		if(phase != RUNNING && phase != LETHAL && phase != END)
			throw new IllegalStateException(msg.main("game.not_stoppable"));

		if(!runTask.isCancelled())
			runTask.cancel();

		stopwatch.stop();

		scoreboardProcessor.stop();

		bedHandler.deactivate();
		mobRepellent.deactivate();

		World defWorld = Bukkit.getWorlds()
				.get(0);
		Location spawn = defWorld.getSpawnLocation();

		for(Gamer gamer : hub.getGamers()) {

			Player player = gamer.getPlayer();

			// TODO: edit offline players, in a way or another
			if(!player.isOnline())
				continue;

			//noinspection ConstantConditions
			player.getAttribute(GENERIC_MAX_HEALTH)
					.setBaseValue(20);
			player.setHealth(20);
			player.setFoodLevel(20);
			player.setGameMode(SURVIVAL);
			player.getInventory()
					.clear();
			player.setLevel(0);

			if(world.equals(player.getWorld()))
				player.teleport(spawn);

		}

		godMode.disable();

		phase = WORN;

		// Call GameStopEvent on game stop
		Bukkit.getScheduler()
				.runTaskAsynchronously(xkUHC.instance(), () -> Bukkit.getPluginManager()
						.callEvent(new GameStopEvent(Game.this)));

	}

	public void unset() throws IllegalStateException {

		if(phase.isRunning())
			throw new IllegalStateException(msg.game("game.not_editable"));

		scoreboardProcessor.untrackAll();

		if(world != null) {

			Bukkit.unloadWorld(world, false);
			world = null;

		}

		hub.unset();

	}

	public @NotNull Map<String, String> dump() {

		LinkedHashMap<String, String> map = new LinkedHashMap<>();

		final String running = msg.game("game.options.running");
		final String still = msg.game("game.options.still");

		map.put("StopwatchStatus", stopwatch.isRunning() ? running : still);
		map.put("StopwatchCount", stopwatch.getTotalSeconds() + "s");

		map.put("MainLoop", (runTask != null && !runTask.isCancelled()) ? running : still);
		map.put("ScoreboardLoop", (runTask != null && !runTask.isCancelled()) ? running : still);
		map.put("EventRaiser", (hub.getEventRaiser()
				.isOn()) ? running : still);

		map.put("WorldName", String.valueOf(world != null ? world.getName() : null));
		map.put("Phase", phase.toString());

		map.put("Gamers Count", String.valueOf(hub.getGamers()
				.size()));
		map.put("Bonds Count", String.valueOf(hub.getBonds()
				.size()));

		return map;
	}

	public enum Phase {

		READY(false),
		STARTING(true),
		RUNNING(true),
		LETHAL(true),
		END(true),
		WORN(false);

		private final boolean running;

		Phase(boolean running) {
			this.running = running;
		}

		public boolean isRunning() {
			return running;
		}

	}

	@EventHandler
	private void onGamerDisconnect(GamerDisconnectEvent event) {

		// TODO: IN-GAME DISCONNECT

	}

	@EventHandler
	private void onGamerReconnect(GamerReconnectEvent event) {

		// TODO: IN-GAME RECONNECT

	}

	@EventHandler
	private void onGamerDeath(GamerDeathEvent event) {

		Gamer gamer = event.getGamer();
		Player player = gamer.getPlayer();

		if(phase.isRunning() && gamer.isAlive() && gamer.hasBond()) {

			World world = player.getWorld();
			Location location = player.getLocation();

			// drop inventory
			for(ItemStack itemStack : player.getInventory())
				if(itemStack != null)
					world.dropItemNaturally(location, itemStack);

			// Play death sound
			player.playSound(player, ENTITY_PLAYER_ATTACK_CRIT, 100, 0);

			// clear inventory after dropping
			player.getInventory()
					.clear();

			Bond bond = gamer.getBond();

			//noinspection ConstantConditions
			player.getAttribute(GENERIC_MAX_HEALTH)
					.setBaseValue(20);
			player.setHealth(20);
			player.setGameMode(SPECTATOR);
			gamer.setAlive(false);

			String message;
			boolean isPK = event.byGamer();

			message = switch(event.getDamageCause()) {
				case PROJECTILE -> msg.game("death.reason.PROJECTILE." + (isPK ? "player" : "default"));
				case ENTITY_ATTACK -> (isPK) ? msg.gameRandomPick("death.reason.ENTITY_ATTACK.player") : msg.game("death.reason.ENTITY_ATTACK.default");
				default -> msg.game("death.reason." + event.getDamageCause());
			};

			MessageFormatter formatter = MessageFormatter.with(gamer, bond);

			if(isPK)
				formatter.killer(event.getKiller());

			// Broadcast death message
			hub.broadcast(formatter.format(message));

			boolean bondDead = bond.getGamers()
					.stream()
					.noneMatch(Gamer::isAlive);

			if(bondDead) {

				hub.broadcast(formatter.format(msg.game("death.bond")));

				List<Bond> bondsLeft = hub.getBonds()
						.stream()
						.filter(bond1 -> bond1.getGamers()
								.stream()
								.anyMatch(Gamer::isAlive))
						.toList();

				if(bondsLeft.size() == 1) {

					Bond winnerBond = bondsLeft.get(0);
					hub.broadcast(formatter.bond(winnerBond)
							.format(msg.game("game.win")));

					end();

				}
			}

		} else
			player.setHealth(0);

	}

}
