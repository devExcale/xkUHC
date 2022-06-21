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
import ovh.excale.xkuhc.comms.PlayerListPrinter;
import ovh.excale.xkuhc.comms.ScoreboardProcessor;
import ovh.excale.xkuhc.configuration.BorderAction;
import ovh.excale.xkuhc.configuration.GameSettings;
import ovh.excale.xkuhc.discord.DiscordEndpoint;
import ovh.excale.xkuhc.eventhandlers.*;
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
import static ovh.excale.xkuhc.core.Stopwatch.timeToString;

public class Game implements Listener {

	private final Logger log;

	private final GamerHub hub;
	private final Stopwatch stopwatch;
	private final MessageBundles msg;

	// GameAccessories
	private final Set<GameAccessory> accessories;

	// Referenced Accessories
	private final GodModeHandler godmodeAccessory;
	private final ScoreboardProcessor scoreboardProcessor;
	private final PlayerListPrinter tabPrinter;

	private BukkitTask runTask;
	private Phase phase;

	private World world;
	private GameSettings settings;
	private Iterator<BorderAction> borderActions;
	private BorderAction currentAction;

	private boolean confirmStart;

	public Game() {
		hub = new GamerHub(this);
		stopwatch = new Stopwatch();

		accessories = Collections.synchronizedSet(new HashSet<>());

		accessories.add(godmodeAccessory = new GodModeHandler());
		accessories.add(scoreboardProcessor = new ScoreboardProcessor());
		accessories.add(tabPrinter = new PlayerListPrinter(hub));
		accessories.add(new MobRepellentHandler(this));
		accessories.add(new MateFindingCompass(hub));
		accessories.add(new BedInteractionHandler());

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

		changePhase(READY);

		initScoreboardProcessor();
		initTabPrinter();

	}

	private void initTabPrinter() {

		tabPrinter.header(gamer -> {

			String borderRadius = Optional.ofNullable(world)
					.map(World::getWorldBorder)
					.map(WorldBorder::getSize)
					.map(size -> size.intValue() / 2)
					.map(String::valueOf)
					.orElse("N/A");

			String actionRemaining = Optional.ofNullable(currentAction)
					.map(BorderAction::getTime)
					.map(time -> time - stopwatch.getLapDelta())
					.map(Stopwatch::timeToString)
					.orElse("N/A");

			String actionType = Optional.ofNullable(currentAction)
					.map(BorderAction::getType)
					.map(BorderAction.ActionType::getMessageKeyShort)
					.map(msg::gameRaw)
					.orElse("N/A");

			//noinspection StringBufferReplaceableByString
			String header = new StringBuilder("\n").append("   --- ")
					.append("{BOLD}Border: {RESET}")
					.append("{ITALIC}{actionType}{RESET}")
					.append(" ---   ")
					.append("\n\n")
					.append("   |  ")
					.append("{BOLD}Radius: {RESET}")
					.append("{ITALIC}{borderRad}{RESET}")
					.append("  |  ")
					.append("{BOLD}Remaining: {RESET}")
					.append("{ITALIC}{actionRemaining}{RESET}")
					.append("  |   ")
					.append("\n")
					.toString();

			return new MessageFormatter(header).addColors()
					.custom("actionType", actionType)
					.custom("actionRemaining", actionRemaining)
					.custom("borderRad", borderRadius)
					.format();
		});

		tabPrinter.footer(gamer -> {

			Location playerLoc = gamer.getPlayer()
					.getLocation();

			//noinspection StringBufferReplaceableByString
			String footer = new StringBuilder("\n").append("   ")
					.append("{BOLD}Coords: {RESET}")
					.append("{ITALIC}[{gamerX} {gamerY} {gamerZ}]{RESET}")
					.append(" | ")
					.append("{BOLD}Time: {RESET}")
					.append("{ITALIC}{gameTime}{RESET}")
					.append("   ")
					.append("\n")
					.toString();

			return new MessageFormatter(footer).addColors()
					.custom("gameTime", timeToString(stopwatch.getTotalSeconds()))
					.custom("gamerX", playerLoc.getBlockX())
					.custom("gamerY", playerLoc.getBlockY())
					.custom("gamerZ", playerLoc.getBlockZ())
					.format();
		});

	}

	private void initScoreboardProcessor() {

		// SCOREBOARD
		// (14)
		// (13) > Bond:
		// (12) > Gamer:
		// (11) > Kills:
		// (10)
		// ( 9)
		// ( 8)
		// ( 7)
		// ( 6)
		// ( 5) > Bonds:
		// ( 4) > Gamers:
		// ( 3)
		// ( 2)
		// ( 1)
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

	}

	private void changePhase(Phase phase) {

		this.phase = phase;

		for(GameAccessory accessory : accessories)
			accessory.onPhaseChange(phase);

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
			throw new IllegalStateException(msg.mainRaw("game.no_bonds"));

		DiscordEndpoint discord = DiscordEndpoint.getInstance();
		if(discord != null) {

			if(!discord.hasMainChannel())
				throw new IllegalStateException(msg.discordRaw("error.no_main_ch"));

			Set<UUID> linked = discord.getLinkedPlayers();
			String unlinked = hub.getGamers()
					.stream()
					.map(Gamer::getPlayer)
					.filter(player -> !linked.contains(player.getUniqueId()))
					.map(Player::getDisplayName)
					.collect(Collectors.joining(", "));

			if(!unlinked.isEmpty() && !confirmStart)
				throw new IllegalStateException(msg.discord("error.unlinked")
						.custom("unlinked", unlinked)
						.format());

		}

		borderActions = settings.getBorderActionIterator();

		Bukkit.getScheduler()
				.runTaskAsynchronously(xkUHC.instance(), this::start);
	}

	private void start() {

		changePhase(STARTING);

		hub.broadcast(msg.gameRaw("game.loading"));

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

		hub.broadcast(msg.gameRaw("game.teleporting"));

		PlayerSpreader spreader = new PlayerSpreader(world, initialBorder - 80);
		AsyncTeleportAnchor tpAnchor = new AsyncTeleportAnchor();

		godmodeAccessory.setIds(hub.getGamers()
				.stream()
				.map(Gamer::getPlayer)
				.map(Entity::getUniqueId)
				.collect(Collectors.toSet()));

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

		hub.broadcastTitle(msg.gameRaw("game.title"), msg.gameRaw("game.subtitle"), 10, 70, 20);
		hub.broadcastSound(ENTITY_EXPERIENCE_ORB_PICKUP, 100, 1);

		runTask = Bukkit.getScheduler()
				.runTaskAsynchronously(xkUHC.instance(), this::run);

		changePhase(RUNNING);

		stopwatch.start();

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
		hub.broadcast(msg.game(actionType.getMessageKeyLong())
				.formatFine());

		runTask = scheduler.runTaskLaterAsynchronously(xkUHC.instance(), borderActions.hasNext() ? this::run : this::end, currentAction.getTime() * 20L);

	}

	/**
	 * Ends the game gracefully, user interactive.
	 */
	public void end() throws IllegalStateException {

		if(phase != RUNNING && phase != LETHAL)
			throw new IllegalStateException(msg.mainRaw("game.not_stoppable"));

		if(!runTask.isCancelled())
			runTask.cancel();

		changePhase(ENDING);

		// TODO: other checks/messages (move win condition over here)

		hub.broadcast(msg.main("game.end_tp")
				.formatFine());

		// TODO: call GameEndEvent

		runTask = Bukkit.getScheduler()
				.runTaskLater(xkUHC.instance(), this::stop, 800);

	}

	/**
	 * Stops abruptly the game, without user interaction.
	 * Needs to be called when the game needs to be stopped instantly.
	 * <br>
	 * (e.g. server disable, plugin deactivation)
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

		if(phase != RUNNING && phase != LETHAL && phase != ENDING)
			throw new IllegalStateException(msg.mainRaw("game.not_stoppable"));

		if(!runTask.isCancelled())
			runTask.cancel();

		stopwatch.stop();

		double[] tpCoords = settings.getTpCoords();
		World tpWorld = Bukkit.getWorld(settings.getTpWorld());
		Location tpLoc;

		if(tpWorld == null) {

			if(tpCoords != null)
				log.warning(msg.mainRaw("error.tp_world_notfound", settings.getTpWorld()));

			tpLoc = Bukkit.getWorlds()
					.get(0)
					.getSpawnLocation();

		} else
			tpLoc = new Location(tpWorld, tpCoords[0], tpCoords[1], tpCoords[2]);

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
				player.teleport(tpLoc);

		}

		changePhase(STOPPED);

		if(settings.isResetAfter()) {

			unset();
			xkUHC.setGame(null);

		}

		// Call GameStopEvent on game disable
		Bukkit.getScheduler()
				.runTaskAsynchronously(xkUHC.instance(), () -> Bukkit.getPluginManager()
						.callEvent(new GameStopEvent(Game.this)));

	}

	public void unset() throws IllegalStateException {

		if(phase.isRunning())
			throw new IllegalStateException(msg.gameRaw("game.not_editable"));

		for(GameAccessory accessory : accessories)
			if(accessory.isEnabled())
				accessory.disable();

		if(world != null) {

			Bukkit.unloadWorld(world, false);
			world = null;

		}

		hub.unset();

	}

	public @NotNull Map<String, String> dump() {

		LinkedHashMap<String, String> map = new LinkedHashMap<>();

		final String running = msg.gameRaw("game.options.running");
		final String still = msg.gameRaw("game.options.still");

		map.put("StopwatchStatus", stopwatch.isRunning() ? running : still);
		map.put("StopwatchCount", stopwatch.getTotalSeconds() + "s");

		map.put("MainLoop", (runTask != null && !runTask.isCancelled()) ? running : still);
		map.put("ScoreboardLoop", (runTask != null && !runTask.isCancelled()) ? running : still);
		map.put("EventRaiser", (hub.getEventRaiser()
				.isEnabled()) ? running : still);

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
		ENDING(true),
		STOPPED(false);

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
				case PROJECTILE -> msg.gameRaw("death.reason.PROJECTILE." + (isPK ? "player" : "default"));
				case ENTITY_ATTACK -> (isPK) ? msg.gameRandomPick("death.reason.ENTITY_ATTACK.player") : msg.gameRaw("death.reason.ENTITY_ATTACK.default");
				default -> msg.gameRaw("death.reason." + event.getDamageCause());
			};

			MessageFormatter formatter = new MessageFormatter(message);

			if(isPK)
				formatter.killer(event.getKiller());

			// Broadcast death message
			hub.broadcast(formatter.gamer(gamer)
					.bond(bond)
					.format());

			boolean bondDead = bond.getGamers()
					.stream()
					.noneMatch(Gamer::isAlive);

			if(bondDead) {

				hub.broadcast(msg.game("death.bond")
						.bond(bond)
						.format());

				List<Bond> bondsLeft = hub.getBonds()
						.stream()
						.filter(bond1 -> bond1.getGamers()
								.stream()
								.anyMatch(Gamer::isAlive))
						.toList();

				if(bondsLeft.size() == 1) {

					Bond winnerBond = bondsLeft.get(0);
					hub.broadcast(msg.game("game.win")
							.bond(winnerBond)
							.format());

					end();

				}
			}

		} else
			player.setHealth(0);

	}

}
