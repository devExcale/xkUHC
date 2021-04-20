package ovh.excale.mc.uhc;

import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ovh.excale.mc.UHC;
import ovh.excale.mc.uhc.core.Bond;
import ovh.excale.mc.uhc.core.Gamer;
import ovh.excale.mc.uhc.core.GamerHub;
import ovh.excale.mc.uhc.core.events.GamerDeathEvent;
import ovh.excale.mc.uhc.core.events.GamerDisconnectEvent;
import ovh.excale.mc.uhc.core.events.GamerReconnectEvent;
import ovh.excale.mc.uhc.misc.BorderAction;
import ovh.excale.mc.uhc.misc.GameSettings;
import ovh.excale.mc.uhc.misc.ScoreboardProcessor;
import ovh.excale.mc.uhc.misc.UhcWorldUtil;
import ovh.excale.mc.utils.PlayerSpreader;
import ovh.excale.mc.utils.Stopwatch;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.BOLD;
import static org.bukkit.ChatColor.RESET;
import static ovh.excale.mc.uhc.misc.BorderAction.ActionType;

public class Game implements Listener {

	private final GamerHub hub;
	private final Stopwatch stopwatch;
	private final ScoreboardProcessor scoreboardProcessor;
	private final YamlConfiguration messages;

	private BukkitTask runTask;
	private Status status;
	// TODO: SET STATUS
	// TODO: STATUS EVENTS

	private World world;
	private GameSettings settings;
	private Iterator<BorderAction> borderActions;
	private BorderAction currentAction;

	public Game(YamlConfiguration messages) {
		hub = new GamerHub(this);
		stopwatch = new Stopwatch();
		scoreboardProcessor = new ScoreboardProcessor();
		this.messages = Objects.requireNonNull(messages);

		runTask = null;
		status = Status.PREPARING;

		world = null;
		settings = null;
		currentAction = null;

		PluginManager pluginManager = Bukkit.getPluginManager();

		// GAMER RECONNECT EVENT
		pluginManager.registerEvent(GamerReconnectEvent.class,
				this,
				EventPriority.HIGH,
				(listener, event) -> ((Game) listener).onGamerReconnect((GamerReconnectEvent) event),
				UHC.plugin());
		// GAMER DISCONNECT EVENT
		pluginManager.registerEvent(GamerDisconnectEvent.class,
				this,
				EventPriority.HIGH,
				(listener, event) -> ((Game) listener).onGamerDisconnect((GamerDisconnectEvent) event),
				UHC.plugin());
		// GAMER DEATH EVENT
		pluginManager.registerEvent(GamerDeathEvent.class,
				this,
				EventPriority.HIGH,
				(listener, event) -> ((Game) listener).onGamerDeath((GamerDeathEvent) event),
				UHC.plugin());

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
		scoreboardProcessor.print(11,
				gamer -> "> " + BOLD + "Kills: " + RESET + gamer.getKillCount());

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

			String action = (currentAction == null) ? "none" : currentAction.getType()
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
		scoreboardProcessor.print(5,
				gamer -> "> " + BOLD + "Bonds: " + RESET + (int) hub.getBonds()
						.stream()
						.filter(Bond::isAlive)
						.count());

		// GAMERS
		scoreboardProcessor.print(4,
				gamer -> "> " + BOLD + "Gamers: " + RESET + (int) hub.getGamers()
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

			return str + String.format(str + "%2dm:%2ds ]", m % 60, s);
		});

		// POSITION
		scoreboardProcessor.print(1, gamer -> {

			Location loc = gamer.getPlayer()
					.getLocation();

			return String.format("[ %d, %d, %d ]",
					loc.getBlockX(),
					loc.getBlockY(),
					loc.getBlockZ());
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
			throw new IllegalStateException("Cannot start game with less than 2 bonds");

		File file = new File(UHC.plugin()
				.getDataFolder(), "lang/game-messages.yml");

		if(!file.exists()) {
			UHC.logger()
					.log(Level.SEVERE, "Cannot read resource game-messages.yml");
			throw new IllegalStateException("Cannot read resource game-messages.yml");
		}

		borderActions = settings.getBorderActionIterator();

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

						for(Gamer gamer : hub.getGamers()) {
							gamer.resetKillCount();
							gamer.getPlayer()
									.addPotionEffect(blindness);
						}

						return Void.TYPE;
					})
					.get();
		} catch(Exception e) {
			UHC.logger()
					.log(Level.SEVERE, e.getMessage(), e);
			hub.broadcast(ChatColor.RED + "An internal error has occurred");
			status = Status.READY;
			return;
		}

		hub.broadcast("Teleporting players...");

		PlayerSpreader spreader = new PlayerSpreader(world, initialBorder - 160);

		for(Bond bond : hub.getBonds()) {

			hub.setEnableFriendlyFire(bond, settings.isFriendlyFireEnabled());
			spreader.spread(bond.getGamers()
					.stream()
					.map(Gamer::getPlayer)
					.toArray(Player[]::new));

			// Wait for chunks to load
			try {
				Thread.sleep(1000);
			} catch(InterruptedException ignored) {
			}

		}

		// Wait for chunks to load
		try {
			Thread.sleep(4000);
		} catch(InterruptedException ignored) {
		}

		Set<Player> players = hub.getGamers()
				.stream()
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
			status = Status.READY;
			return;
		}

		for(Player player : players)
			player.sendTitle("UHC", "Let the ^^^ start!", 10, 70, 20);

		runTask = Bukkit.getScheduler()
				.runTaskAsynchronously(UHC.plugin(), this::run);
		status = Status.RUNNING;
		stopwatch.start();

	}

	private void run() {

		stopwatch.lap();
		currentAction = borderActions.next();
		BukkitScheduler scheduler = Bukkit.getScheduler();

		Sound sound;

		if(currentAction.getType() == ActionType.SHRINK) {
			world.getWorldBorder()
					.setSize(currentAction.getBorderSize(), currentAction.getMinutes() * 60L);
			sound = Sound.ENTITY_PLAYER_BREATH;
		} else
			sound = Sound.BLOCK_ANVIL_PLACE;

		for(Gamer gamer : hub.getGamers()) {
			Player player = gamer.getPlayer();

			player.playSound(player.getLocation(), sound, 1, 1);
			player.sendMessage(currentAction.getType()
					.getMessage());

		}

		runTask = scheduler.runTaskLaterAsynchronously(UHC.plugin(),
				borderActions.hasNext() ? this::run : this::endgame,
				currentAction.getMinutes() * 1200L);

	}

	private void endgame() {
		// TODO: ENDGAME
	}

	public void stop() throws IllegalStateException {

		if(!status.equals(Status.RUNNING))
			throw new IllegalStateException("Game ain't running");

		if(!runTask.isCancelled())
			runTask.cancel();
		stopwatch.stop();
		scoreboardProcessor.stop();

		// TODO: AND_THEN CALLABLE
		hub.broadcast("Teleporting all players back in 40 seconds...");

		Bukkit.getScheduler()
				.runTaskLater(UHC.plugin(), () -> {

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
	}

	public void dispose() throws IllegalStateException {

		if(!status.isEditable())
			throw new IllegalStateException(messages.getString("game.not_editable"));

		scoreboardProcessor.untrackAll();

		if(world != null)
			Bukkit.unloadWorld(world, false);
		hub.dispose();

	}

	public @NotNull Map<String, String> dump() {

		LinkedHashMap<String, String> map = new LinkedHashMap<>();

		final String running = messages.getString("game.options.running");
		final String still = messages.getString("game.options.still");

		map.put("StopwatchStatus", stopwatch.isRunning() ? running : still);
		map.put("StopwatchCount", stopwatch.getTotalSeconds() + "s");

		map.put("MainLoop", (runTask != null && !runTask.isCancelled()) ? running : still);
		map.put("ScoreboardLoop", (runTask != null && !runTask.isCancelled()) ? running : still);
		map.put("EventRaiser",
				(hub.getEventRaiser()
						.isOn()) ? running : still);

		map.put("WorldName", String.valueOf(world != null ? world.getName() : null));
		map.put("Status", status.toString());

		map.put("Gamers Count",
				String.valueOf(hub.getGamers()
						.size()));
		map.put("Bonds Count",
				String.valueOf(hub.getBonds()
						.size()));

		return map;
	}

	public enum Status {

		PREPARING(true),
		READY(true),
		STARTING(false),
		RUNNING(false),
		FINAL(false),
		WORN(true);

		private final boolean editable;

		Status(boolean editable) {
			this.editable = editable;
		}

		@SuppressWarnings("BooleanMethodIsAlwaysInverted")
		public boolean isEditable() {
			return editable;
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
	void onGamerDeath(GamerDeathEvent event) {

		Gamer gamer = event.getGamer();
		Player player = gamer.getPlayer();

		player.getInventory()
				.forEach(itemStack -> player.getWorld()
						.dropItemNaturally(player.getLocation(), itemStack));

		if(status.equals(Status.RUNNING) && gamer.isAlive() && gamer.hasBond()) {

			Bond bond = gamer.getBond();

			//noinspection ConstantConditions
			player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
					.setBaseValue(20);
			player.setHealth(20);
			player.setGameMode(GameMode.SPECTATOR);
			gamer.setAlive(false);

			String message;
			boolean isPK = event.byGamer();

			switch(event.getDamageCause()) {

				case PROJECTILE:
					if(isPK)
						message = messages.getString("death.PROJECTILE.player", "?");
					else
						message = messages.getString("death.PROJECTILE.default", "?");
					break;

				case ENTITY_ATTACK:
					if(isPK) {
						List<String> list = messages.getStringList("death.ENTITY_ATTACK.player");
						if(list.isEmpty())
							message = "?";
						else
							message = list.get(new Random().nextInt(list.size()));
					} else
						message = messages.getString("death.ENTITY_ATTACK.default", "?");
					break;

				default:
					message = messages.getString("death." + event.getDamageCause(), "?");

			}

			ChatColor bondColor = bond.getColor();
			String gamerName = player.getName();

			message = Objects.requireNonNull(message)
					.replaceAll("\\{gamer}", bondColor.toString() + gamerName + ChatColor.WHITE);
			if(isPK) {
				Gamer killer = event.getKiller();
				Player killerPlayer = killer.getPlayer();
				Bond killerBond = killer.getBond();
				ChatColor killerColor = killerBond.getColor();
				String killerName = killerPlayer.getName();
				message = message.replaceAll("\\{killer}",
						killerColor.toString() + killerName + ChatColor.WHITE);
			}

			// Broadcast death message
			hub.broadcast(message);

			boolean bondDead = bond.getGamers()
					.stream()
					.noneMatch(Gamer::isAlive);

			if(bondDead) {

				hub.broadcast("Bond " + bond.getColor() + bond.getName() + RESET + " has been broken!");

				List<Bond> bondsLeft = hub.getBonds()
						.stream()
						.filter(bond1 -> bond1.getGamers()
								.stream()
								.anyMatch(Gamer::isAlive))
						.collect(Collectors.toList());

				if(bondsLeft.size() == 1) {

					Bond winnerBond = bondsLeft.get(0);
					hub.broadcast("There only is one bond remaining. Bond " + winnerBond.getColor() + winnerBond.getName() + RESET + " wins!");

					stop();

				}
			}
		}

	}

}
