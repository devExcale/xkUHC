package ovh.excale.mc.uhc;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ovh.excale.discord.DiscordEndpoint;
import ovh.excale.mc.UHC;
import ovh.excale.mc.eventhandlers.AsyncTeleportAnchor;
import ovh.excale.mc.eventhandlers.BedInteractionHandler;
import ovh.excale.mc.eventhandlers.MobRepellentHandler;
import ovh.excale.mc.uhc.core.Bond;
import ovh.excale.mc.uhc.core.Gamer;
import ovh.excale.mc.uhc.core.GamerHub;
import ovh.excale.mc.uhc.core.events.*;
import ovh.excale.mc.uhc.misc.BorderAction;
import ovh.excale.mc.uhc.misc.GameSettings;
import ovh.excale.mc.uhc.misc.ScoreboardProcessor;
import ovh.excale.mc.uhc.world.WorldManager;
import ovh.excale.mc.utils.MessageBundles;
import ovh.excale.mc.utils.MessageFormatter;
import ovh.excale.mc.utils.PlayerSpreader;
import ovh.excale.mc.utils.Stopwatch;

import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.BOLD;
import static org.bukkit.ChatColor.RESET;
import static org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
import static ovh.excale.mc.uhc.Game.Status.RUNNING;
import static ovh.excale.mc.uhc.Game.Status.STARTING;
import static ovh.excale.mc.uhc.misc.BorderAction.ActionType;

public class Game implements Listener {

	private final GamerHub hub;
	private final Stopwatch stopwatch;
	private final ScoreboardProcessor scoreboardProcessor;
	private final MessageBundles msg;

	private final BedInteractionHandler bedHandler;
	private final MobRepellentHandler mobRepellent;

	private BukkitTask runTask;
	private Status status;
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

		msg = UHC.instance()
				.getMessages();

		runTask = null;
		status = Status.PREPARING;

		world = null;
		settings = null;
		currentAction = null;
		confirmStart = false;

		PluginManager pluginManager = Bukkit.getPluginManager();

		// GAMER RECONNECT EVENT
		pluginManager.registerEvent(GamerReconnectEvent.class, this, EventPriority.HIGH, (listener, event) -> ((Game) listener).onGamerReconnect((GamerReconnectEvent) event),
				UHC.instance());
		// GAMER DISCONNECT EVENT
		pluginManager.registerEvent(GamerDisconnectEvent.class, this, EventPriority.HIGH, (listener, event) -> ((Game) listener).onGamerDisconnect((GamerDisconnectEvent) event),
				UHC.instance());
		// GAMER DEATH EVENT
		pluginManager.registerEvent(GamerDeathEvent.class, this, EventPriority.HIGH, (listener, event) -> ((Game) listener).onGamerDeath((GamerDeathEvent) event), UHC.instance());

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
		// ( 8) > Status:
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

			return "> " + BOLD + "Status: " + RESET + action;
		});

		// REMAINING
		scoreboardProcessor.print(7, gamer -> {

			String s = "> " + BOLD + "Remaining:" + RESET + " N/A";
			if(currentAction != null)
				s = "> " + BOLD + "Remaining: " + RESET + (currentAction.getMinutes() * 60 - stopwatch.getLapDelta()) + "s";

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

	public Status getStatus() {
		return status;
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

	public Game setConfirmStart(boolean confirm) {
		confirmStart = confirm;
		return this;
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
				.runTaskAsynchronously(UHC.instance(), this::start);
	}

	private void start() {

		status = STARTING;

		hub.broadcast(msg.game("game.loading"));

		world = new WorldManager().loadSpawn(false)
				.generateUntilClearCenter()
				.applyRules()
				.getWorld();

		//noinspection ConstantConditions
		WorldBorder border = world.getWorldBorder();
		int initialBorder = settings.getInitialBorderSize();

		Bukkit.getScheduler()
				.callSyncMethod(UHC.instance(), () -> {
					border.setSize(initialBorder);
					return null;
				});

		// broadcast world stats

		for(Gamer gamer : hub.getGamers())
			gamer.initForGame(true);

		hub.broadcast(msg.game("game.teleporting"));

		PlayerSpreader spreader = new PlayerSpreader(world, initialBorder - 80);
		AsyncTeleportAnchor tpAnchor = new AsyncTeleportAnchor();

		Bukkit.getScheduler()
				.callSyncMethod(UHC.instance(), () -> {

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
				.callSyncMethod(UHC.instance(), () -> {

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
				.runTaskAsynchronously(UHC.instance(), this::run);
		status = RUNNING;

		stopwatch.start();
		bedHandler.activate();
		mobRepellent.activate();

	}

	private void run() {

		stopwatch.lap();
		currentAction = borderActions.next();
		BukkitScheduler scheduler = Bukkit.getScheduler();

		Sound sound;

		if(currentAction.getType() == ActionType.SHRINK) {

			WorldBorder border = world.getWorldBorder();
			Bukkit.getScheduler()
					.callSyncMethod(UHC.instance(), () -> {
						border.setSize(currentAction.getBorderSize(), currentAction.getMinutes() * 60L);
						return null;
					});

			sound = Sound.ENTITY_PLAYER_BREATH;

		} else
			sound = Sound.BLOCK_ANVIL_PLACE;

		for(Gamer gamer : hub.getGamers()) {
			Player player = gamer.getPlayer();

			player.playSound(player.getLocation(), sound, 1, 1);
			player.sendMessage(currentAction.getType()
					.getMessage());

		}

		runTask = scheduler.runTaskLaterAsynchronously(UHC.instance(), borderActions.hasNext() ? this::run : this::endgame, currentAction.getMinutes() * 1200L);

	}

	private void endgame() {
		// TODO: ENDGAME
	}

	public void stop() throws IllegalStateException {

		if(!status.equals(RUNNING))
			throw new IllegalStateException(msg.main("game.not_running"));

		if(!runTask.isCancelled())
			runTask.cancel();

		stopwatch.stop();
		scoreboardProcessor.stop();

		bedHandler.deactivate();
		mobRepellent.deactivate();

		hub.broadcast(msg.main("game.end_tp"));

		// TODO: FIX: CANNOT STOP ON PLUGIN DISABLE (cannot schedule task while plugin is disabled)
		Bukkit.getScheduler()
				.runTaskLater(UHC.instance(), () -> {

					World defWorld = Bukkit.getWorlds()
							.get(0);
					Location spawn = defWorld.getSpawnLocation();

					for(Gamer gamer : hub.getGamers()) {
						Player player = gamer.getPlayer();

						//noinspection ConstantConditions
						player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
								.setBaseValue(20);
						player.setHealth(20);
						player.setFoodLevel(20);
						player.setGameMode(GameMode.SURVIVAL);
						player.teleport(spawn);
						player.getInventory()
								.clear();
						player.setLevel(0);

					}

				}, 800);

		status = Status.WORN;

		// Call GameStopEvent on game stop
		Bukkit.getScheduler()
				.runTaskAsynchronously(UHC.instance(), () -> Bukkit.getPluginManager()
						.callEvent(new GameStopEvent(Game.this)));

	}

	public void dispose() throws IllegalStateException {

		if(!status.isEditable())
			throw new IllegalStateException(msg.game("game.not_editable"));

		scoreboardProcessor.untrackAll();

		if(world != null)
			Bukkit.unloadWorld(world, false);
		hub.dispose();

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
		map.put("Status", status.toString());

		map.put("Gamers Count", String.valueOf(hub.getGamers()
				.size()));
		map.put("Bonds Count", String.valueOf(hub.getBonds()
				.size()));

		return map;
	}

	public enum Status {

		PREPARING(true, true),
		READY(true, true),
		STARTING(false, false),
		RUNNING(false, false),
		FINAL(false, false),
		WORN(false, true);

		private final boolean editable;
		private final boolean deletable;

		Status(boolean editable, boolean deletable) {
			this.editable = editable;
			this.deletable = deletable;
		}

		@SuppressWarnings("BooleanMethodIsAlwaysInverted")
		public boolean isEditable() {
			return editable;
		}

		public boolean isDeletable() {
			return deletable;
		}

		public boolean isStoppable() {
			return !editable & !deletable;
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

		if(status.equals(RUNNING) && gamer.isAlive() && gamer.hasBond()) {

			World world = player.getWorld();
			Location location = player.getLocation();

			// drop inventory
			for(ItemStack itemStack : player.getInventory())
				if(itemStack != null)
					world.dropItemNaturally(location, itemStack);

			// Play death sound
			player.playSound(player, Sound.ENTITY_PLAYER_DEATH, 100, 0);

			// clear inventory after dropping
			player.getInventory()
					.clear();

			Bond bond = gamer.getBond();

			//noinspection ConstantConditions
			player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
					.setBaseValue(20);
			player.setHealth(20);
			player.setGameMode(GameMode.SPECTATOR);
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

					stop();

				}
			}
		} else
			player.setHealth(0);

	}

}
