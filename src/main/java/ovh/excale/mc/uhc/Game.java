package ovh.excale.mc.uhc;

import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ovh.excale.mc.UHC;
import ovh.excale.mc.uhc.core.Gamer;
import ovh.excale.mc.uhc.core.GamerHub;
import ovh.excale.mc.uhc.misc.BorderAction;
import ovh.excale.mc.uhc.misc.GameSettings;
import ovh.excale.mc.utils.PlayerSpreader;
import ovh.excale.mc.utils.ScoreboardPrinter;
import ovh.excale.mc.utils.Stopwatch;
import ovh.excale.mc.utils.UhcWorldUtil;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static ovh.excale.mc.uhc.misc.BorderAction.ActionType;

public class Game implements Listener {

	// TODO: SCOREBOARD PROCESSOR
	private final GamerHub hub;
	private final Stopwatch stopwatch;

	private BukkitTask runTask;
	private BukkitTask sidebarUpdater;
	private Status status;
	// TODO: SET STATUS
	// TODO: STATUS EVENTS

	private World world;
	private GameSettings settings;
	private Iterator<BorderAction> borderActions;
	private BorderAction currentAction;

	protected Game() {
		hub = new GamerHub(this);
		status = Status.PREPARING;
		stopwatch = new Stopwatch();
		runTask = null;
		world = null;

//		// REGISTER EVENTS LISTENER
//		PluginManager pluginManager = Bukkit.getPluginManager();
//		pluginManager.registerEvent(PlayerJoinEvent.class,
//				this,
//				EventPriority.HIGH,
//				(listener, event) -> ((Game) listener).onPlayerJoin((PlayerJoinEvent) event),
//				UHC.plugin());
//		pluginManager.registerEvent(PlayerQuitEvent.class,
//				this,
//				EventPriority.HIGH,
//				(listener, event) -> ((Game) listener).onPlayerQuit((PlayerQuitEvent) event),
//				UHC.plugin());

	}

	public void tryStart() throws IllegalStateException {

		settings = GameSettings.fromConfig();
		if(!settings.isLegal())
			throw new IllegalStateException(settings.getErrorMessage());

		Set<Gamer> removableGamers = hub.getGamers()
				.filter(gamer -> !gamer.isOnline())
				.collect(Collectors.toSet());

		for(Gamer gamer : removableGamers)
			hub.unregister(gamer);

		hub.getBonds()
				.filter(bond -> bond.size() == 0)
				.forEach(bond -> hub.removeBond(bond.getName()));

		if(hub.getBonds()
				.count() < 2)
			throw new IllegalStateException("Cannot start game with less than 2 bonds");

		Bukkit.getScheduler()
				.runTaskAsynchronously(UHC.plugin(), this::start);

	}

	private void start() {

		hub.broadcast("Loading game...");
		status = Status.STARTING;
		hub.broadcast("Generating world, server may lag...");

		Optional<World> optional = UhcWorldUtil.generate();
		while(!optional.isPresent()) {

			// Wait for the server to catch-up
			try {
				//noinspection BusyWait
				Thread.sleep(1000L);
			} catch(Exception ignored) {
			}

			hub.broadcast("World generation failed. Generating again...");
			optional = UhcWorldUtil.generate();

		}

		world = optional.get();
		WorldBorder border = world.getWorldBorder();
		int initialBorder = settings.getInitialBorderSize();
		border.setSize(initialBorder);

		hub.broadcast("World generated!\n - WorldName: " + world.getName() + "\n - BorderSize: " + initialBorder);

		PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS,
				3600,
				12,
				false,
				false,
				false);
		try {
			Bukkit.getScheduler()
					.callSyncMethod(UHC.plugin(), () -> {
						hub.getGamers()
								.map(Gamer::getPlayer)
								.forEach(player -> player.addPotionEffect(blindness));
						return Void.TYPE;
					})
					.get();
		} catch(Exception e) {
			UHC.logger()
					.log(Level.SEVERE, e.getMessage(), e);
			hub.broadcast(ChatColor.RED + "An internal error has occurred");
			// TODO: ERROR STATUS
			return;
		}


		hub.broadcast("Teleporting players...");

		PlayerSpreader spreader = new PlayerSpreader(world, initialBorder - 160);

		hub.getBonds()
				.forEach(bond -> {

					spreader.spread(bond.getGamers()
							.map(Gamer::getPlayer)
							.toArray(Player[]::new));

					// Wait for chunks to load
					try {
						Thread.sleep(1000);
					} catch(InterruptedException ignored) {
					}

				});

		// Wait for chunks to load
		try {
			Thread.sleep(4000);
		} catch(InterruptedException ignored) {
		}

		Set<Player> players = hub.getGamers()
				.map(Gamer::getPlayer)
				.collect(Collectors.toSet());

		Bukkit.getScheduler()
				.runTaskLater(UHC.plugin(),
						() -> players.forEach(player -> player.setHealth(40)),
						1);

		for(Player player : players) {

			//noinspection ConstantConditions
			player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
					.setBaseValue(40);
			player.setFoodLevel(20);
			player.setHealth(39);
			player.setLevel(0);
			player.getInventory()
					.clear();

			// REVOKE ALL ADVANCEMENTS
			Iterator<Advancement> iter = Bukkit.advancementIterator();
			while(iter.hasNext()) {
				Advancement advancement = iter.next();
				AdvancementProgress progress = player.getAdvancementProgress(advancement);
				progress.getRemainingCriteria()
						.forEach(progress::revokeCriteria);
			}

		}

		try {
			Bukkit.getScheduler()
					.callSyncMethod(UHC.plugin(), () -> {

						for(Player player : players) {
							player.setGameMode(GameMode.SURVIVAL);
							for(PotionEffect effect : player.getActivePotionEffects())
								player.removePotionEffect(effect.getType());
						}

						return Void.TYPE;
					})
					.get();
		} catch(Exception e) {
			UHC.logger()
					.log(Level.SEVERE, e.getMessage(), e);
			hub.broadcast(ChatColor.RED + "An internal error has occurred");
			return;
		}

		for(Player player : players)
			player.sendTitle("UHC", "Let the ^^^ start!", 10, 70, 20);

		// TODO: EVENTS
//		freeze();
//		Bukkit.getPluginManager()
//				.registerEvent(EntityDeathEvent.class, this, EventPriority.HIGH, (listener, event) -> {
//					if(event instanceof PlayerDeathEvent)
//						((Game) listener).onPlayerDeath((PlayerDeathEvent) event);
//				}, UHC.plugin());


		runTask = Bukkit.getScheduler()
				.runTaskAsynchronously(UHC.plugin(), this::run);
		sidebarUpdater = Bukkit.getScheduler()
				.runTaskTimer(UHC.plugin(),
						() -> hub.getGamers()
								.map(Gamer::getScoreboardPrinter)
								.forEach(ScoreboardPrinter::update),
						0L,
						20L);
		stopwatch.start();
		status = Status.RUNNING;

	}

	// TODO: PING SOUND ON
	private void run() {

		stopwatch.lap();
		currentAction = borderActions.next();
		BukkitScheduler scheduler = Bukkit.getScheduler();

		if(currentAction.getType() == ActionType.SHRINK)
			world.getWorldBorder()
					.setSize(currentAction.getBorderSize(), currentAction.getMinutes() * 60);

		hub.broadcast(ActionType.HOLD.getMessage());
		runTask = scheduler.runTaskLaterAsynchronously(UHC.plugin(),
				borderActions.hasNext() ? this::run : this::endgame,
				currentAction.getMinutes() * 1200);

	}

	private void endgame() {
		// TODO: ENDGAME
	}

	private void updateScoreboard() {
		// TODO: SCOREBOARD PROCESSOR
	}

//	public void unset() throws IllegalStateException {
//
//		PlayerQuitEvent.getHandlerList()
//				.unregister(this);
//		PlayerJoinEvent.getHandlerList()
//				.unregister(this);
//		EntityDeathEvent.getHandlerList()
//				.unregister(this);
//
//		for(Gamer gamer : gamers.values())
//			gamer.setBond(null);
//
//		for(Team team : scoreboard.getTeams())
//			team.unregister();
//
//	}

	public void stop() throws IllegalStateException {

		if(!runTask.isCancelled())
			runTask.cancel();
		if(!sidebarUpdater.isCancelled())
			sidebarUpdater.cancel();
		stopwatch.stop();

		hub.broadcast("Teleporting all players back in 40 seconds...");

		Set<Player> players = hub.getGamers()
				.map(Gamer::getPlayer)
				.collect(Collectors.toCollection(HashSet::new));

		Bukkit.getScheduler()
				.runTaskLater(UHC.plugin(), () -> {

					World defWorld = Bukkit.getWorlds()
							.get(0);
					Location spawn = defWorld.getSpawnLocation();

					for(Player player : players) {

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

		hub.dispose();
		// TODO: DISPOSE ALL

	}

	public @NotNull Map<String, String> dump() {

		LinkedHashMap<String, String> map = new LinkedHashMap<>();

		map.put("WorldName", String.valueOf(world == null ? null : world.getName()));
		map.put("Gamers Count",
				String.valueOf(hub.getGamers()
						.count()));
		map.put("Bonds Count",
				String.valueOf(hub.getBonds()
						.count()));
		map.put("Status", status.toString());
		map.put("Running", String.valueOf(runTask != null && !runTask.isCancelled()));
		map.put("Minutes", String.valueOf(minutes));

		return map;
	}

	public enum Status {

		PREPARING(true),
		READY(false),
		STARTING(false),
		RUNNING(false),
		FINAL(false),
		WORN(true);

		private final boolean editable;

		Status(boolean editable) {
			this.editable = editable;
		}

		public boolean isEditable() {
			return editable;
		}

	}

// TODO: GAMER EVENTS HANDLING
//	@EventHandler
//	void onPlayerQuit(PlayerQuitEvent event) {
//
//		Player player = event.getPlayer();
//		Gamer gamer = gamers.get(player.getUniqueId());
//
//		if(gamer != null) {
//
//			// TODO: ON_PLAYER_QUIT DURING GAME
//
//		}
//	}

// TODO: GAMER EVENTS HANDLING
//	@EventHandler
//	void onPlayerJoin(PlayerJoinEvent event) {
//
//		Player player = event.getPlayer();
//		Gamer gamer = gamers.get(player.getUniqueId());
//
//		if(gamer != null) {
//			gamer.updateReference(player);
//
//			// TODO: ON_PLAYER_JOIN DURING GAME
//
//		}
//	}

// TODO: GAMER EVENTS HANDLING
//	@EventHandler
//	void onPlayerDeath(PlayerDeathEvent event) {
//
//		Player player = event.getEntity();
//		Gamer gamer = getGamer(player);
//
//		// TODO: GET DEATH REASON
//
//		if(gamer != null && gamer.isAlive() && gamer.hasBond()) {
//
//			gamer.setAlive(false);
//			Bond bond = gamer.getBond();
//
//			//noinspection ConstantConditions
//			broadcast("Gamer " + bond.getColor() + player.getDisplayName() + ChatColor.RESET + " has died!");
//
//			Location location = player.getLocation();
//			Bukkit.getScheduler()
//					.runTaskLater(UHC.plugin(), () -> {
//						//noinspection ConstantConditions
//						player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
//								.setBaseValue(20);
//						player.setHealth(20);
//						player.teleport(location);
//						player.setGameMode(GameMode.SPECTATOR);
//					}, 1);
//
//			boolean bondDead = bond.getGamers()
//					.stream()
//					.noneMatch(Gamer::isAlive);
//
//			if(bondDead) {
//
//				broadcast("Bond " + bond.getColor() + bond.getName() + ChatColor.RESET + " has been broken!");
//
//				List<Bond> bondsLeft = bonds.values()
//						.stream()
//						.filter(bond1 -> bond1.getGamers()
//								.stream()
//								.anyMatch(Gamer::isAlive))
//						.collect(Collectors.toList());
//
//				if(bondsLeft.size() == 1) {
//
//					Bond winnerBond = bondsLeft.get(0);
//
//					broadcast("There only is one bond remaining. Bond " + winnerBond.getColor() + winnerBond.getName() + ChatColor.RESET + " wins!");
//					stop();
//
//				}
//
//			}
//
//		}
//
//	}

}