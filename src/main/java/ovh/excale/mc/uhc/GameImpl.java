package ovh.excale.mc.uhc;

import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.UHC;
import ovh.excale.mc.core.Bond;
import ovh.excale.mc.core.BondManager;
import ovh.excale.mc.core.Game;
import ovh.excale.mc.core.Gamer;
import ovh.excale.mc.utils.PlayerSpreader;
import ovh.excale.mc.utils.UhcWorldUtil;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class GameImpl implements Game, BondManager, Listener {

	private final Map<UUID, GamerImpl> gamers;
	private final Map<String, BondImpl> bonds;
	private final Scoreboard scoreboard;
	private boolean frozen;
	private boolean configOk;

	// TODO: DO_INSOMNIA FALSE IN WORLD
	private World world;
	private final Map<Integer, int[]> borders;
	private int initialBorder;
	private int criticalTime;
	private int minutes;
	private BukkitTask runTask;

	protected GameImpl() {
		gamers = Collections.synchronizedMap(new HashMap<>());
		bonds = Collections.synchronizedMap(new HashMap<>());
		borders = new HashMap<>();
		configOk = false;
		frozen = false;
		initialBorder = 0xFFFFFFFF;
		criticalTime = -1;
		minutes = -1;
		runTask = null;

		//noinspection ConstantConditions
		scoreboard = Bukkit.getScoreboardManager()
				.getNewScoreboard();

		// SHOW HEARTS ON PLAYER_LIST
		Objective playerList = scoreboard.registerNewObjective("health_display", "health", "xkUHC", RenderType.HEARTS);
		playerList.setDisplaySlot(DisplaySlot.PLAYER_LIST);

		// REGISTER EVENTS LISTENER
		PluginManager pluginManager = Bukkit.getPluginManager();
		pluginManager.registerEvent(PlayerJoinEvent.class,
				this,
				EventPriority.HIGH,
				(listener, event) -> ((GameImpl) listener).onPlayerJoin((PlayerJoinEvent) event),
				UHC.plugin());
		pluginManager.registerEvent(PlayerQuitEvent.class,
				this,
				EventPriority.HIGH,
				(listener, event) -> ((GameImpl) listener).onPlayerQuit((PlayerQuitEvent) event),
				UHC.plugin());

	}

	private void frozenCheck() throws IllegalStateException {

		if(frozen)
			throw new IllegalStateException("This game won't take any more changes");

	}

	@Override
	public @NotNull Game getGame() {
		return this;
	}

	@Override
	public @NotNull Bond createBond(@NotNull String bondName) throws IllegalStateException, IllegalArgumentException {

		frozenCheck();
		Objects.requireNonNull(bondName);

		BondImpl bond = bonds.get(bondName);

		if(bond != null)
			throw new IllegalArgumentException("A bond with that name already exists");

		bond = new BondImpl(bondName, this);
		bonds.put(bondName, bond);

		return bond;
	}

	@Override
	public void breakBond(@NotNull String bondName) throws IllegalStateException, IllegalArgumentException {

		BondImpl bond = bonds.get(bondName);
		if(bond == null)
			throw new IllegalArgumentException("No bond found for that name");

		bonds.remove(bondName);
		bond.breakBond();

	}

	@Override
	public @Nullable Bond getBond(@Nullable String bondName) {
		return bondName == null ? null : bonds.get(bondName);
	}

	@Override
	public Set<BondImpl> getBonds() {
		return new HashSet<>(bonds.values());
	}

	@Override
	public @NotNull GamerImpl register(@NotNull Player player) throws IllegalStateException, IllegalArgumentException {

		frozenCheck();
		GamerImpl gamer = gamers.get(player.getUniqueId());

		if(gamer != null)
			throw new IllegalArgumentException("This player is already registered");

		gamer = new GamerImpl(player);
		gamers.put(gamer.getUniqueId(), gamer);

		return gamer;
	}

	@Override
	public @Nullable GamerImpl getGamer(@Nullable Player player) {

		return player == null ? null : gamers.get(player.getUniqueId());

	}

	@Override
	public Set<GamerImpl> getGamers() {
		return new HashSet<>(gamers.values());
	}

	@Override
	public Set<Player> getPlayers() {
		return gamers.values()
				.stream()
				.map(GamerImpl::getPlayer)
				.collect(Collectors.toCollection(HashSet::new));
	}

	@Override
	public @NotNull BondManager getBondManager() {
		return this;
	}

	@Override
	public void broadcast(String message) {

		if(message != null)
			for(GamerImpl gamer : gamers.values())
				if(gamer.hasBond())
					gamer.getPlayer()
							.sendMessage(message);

	}

	@Override
	public void tryStart() throws IllegalStateException {

		frozenCheck();
		reloadConfig();

		Set<UUID> removable = new HashSet<>();
		for(Gamer gamer : gamers.values())
			if(!gamer.isOnline()) {

				if(gamer.hasBond())
					//noinspection ConstantConditions TODO: CONTRACTS
					gamer.getBond()
							.unboundGamer(gamer);
				removable.add(gamer.getUniqueId());

			}

		for(UUID uuid : removable)
			gamers.remove(uuid);

		for(BondImpl bond : bonds.values())
			if(bond.getGamers()
					.isEmpty())
				breakBond(bond.getName());

		if(bonds.size() < 2)
			throw new IllegalStateException("Cannot start game with less than 2 bonds");

		Bukkit.getScheduler()
				.runTaskAsynchronously(UHC.plugin(), this::start);

	}

	private void start() {

		broadcast("Loading game...");

		broadcast("Generating world, server may lag...");

		Optional<World> optional = UhcWorldUtil.generate();
		while(!optional.isPresent()) {

			// ~Wait for the server to catch-up
			try {
				//noinspection BusyWait
				Thread.sleep(1000L);
			} catch(InterruptedException ignored) {
			}

			broadcast("World generation failed. Generating again...");
			optional = UhcWorldUtil.generate();

		}

		world = optional.get();
		WorldBorder border = world.getWorldBorder();
		border.setSize(initialBorder);

		broadcast("World generated!\n - WorldName: " + world.getName() + "\n - BorderSize: " + initialBorder);

		Set<Player> players = getPlayers();

		PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 3600, 12, false, false, false);
		try {
			Bukkit.getScheduler()
					.callSyncMethod(UHC.plugin(), () -> {
						for(Player player : players)
							player.addPotionEffect(blindness);
						return Void.TYPE;
					})
					.get();
		} catch(Exception e) {
			UHC.logger()
					.log(Level.SEVERE, e.getMessage(), e);
			broadcast(ChatColor.RED + "An internal error has occurred");
			return;
		}


		broadcast("Teleporting players...");

		PlayerSpreader spreader = new PlayerSpreader(world, initialBorder - 160);

		for(BondImpl bond : bonds.values()) {
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

		Bukkit.getScheduler()
				.runTaskLater(UHC.plugin(), () -> players.forEach(player -> player.setHealth(40)), 1);

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
			broadcast(ChatColor.RED + "An internal error has occurred");
			return;
		}

		for(Player player : players)
			player.sendTitle("UHC", "Let the ^^^ start!", 10, 70, 20);

		freeze();
		Bukkit.getPluginManager()
				.registerEvent(EntityDeathEvent.class, this, EventPriority.HIGH, (listener, event) -> {
					if(event instanceof PlayerDeathEvent)
						((GameImpl) listener).onPlayerDeath((PlayerDeathEvent) event);
				}, UHC.plugin());

		runTask = Bukkit.getScheduler()
				.runTaskTimerAsynchronously(UHC.plugin(), this::run, 0L, 1200L);

	}

	private void run() {

		minutes++;

		int[] borderChange = borders.get(minutes);
		if(borderChange != null) {

			int size = borderChange[0], shrinkTime = borderChange[1];
			int currentSize = (int) world.getWorldBorder()
					.getSize();

			broadcast("The border will shrink from " + currentSize + " to " + size + " for " + (double) shrinkTime / 60 + " minutes!");
			world.getWorldBorder()
					.setSize(size, shrinkTime);
			Bukkit.getScheduler()
					.runTaskLaterAsynchronously(UHC.plugin(), () -> broadcast("The border has stopped shrinking"), shrinkTime * 20L);

		}

	}

	@Override
	public void unset() throws IllegalStateException {

		PlayerQuitEvent.getHandlerList()
				.unregister(this);
		PlayerJoinEvent.getHandlerList()
				.unregister(this);
		EntityDeathEvent.getHandlerList()
				.unregister(this);

		for(GamerImpl gamer : gamers.values())
			gamer.setBond(null);

		for(Team team : scoreboard.getTeams())
			team.unregister();

	}

	@Override
	public void stop() throws IllegalStateException {

		if(!runTask.isCancelled())
			runTask.cancel();

		broadcast("Teleporting all players back in 40 seconds...");

		Set<Player> players = gamers.values()
				.stream()
				.map(GamerImpl::getPlayer)
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

		unset();

	}

	@Override
	public void freeze() throws IllegalStateException {

		frozenCheck();
		frozen = true;

		for(BondImpl bond : bonds.values())
			try {
				bond.freeze();
			} catch(IllegalStateException ignored) {
			}

	}

	@Override
	public void reloadConfig() throws IllegalStateException {

		frozenCheck();
		configOk = false;
		borders.clear();
		criticalTime = -1;
		initialBorder = 0xFFFFFFFF;

		ConfigurationSection config = UHC.plugin()
				.getConfig();

		Integer criticalTime = config.getObject("uhc.CriticalTime", Integer.class);
		if(criticalTime == null)
			throw new IllegalStateException("Cannot find uhc.CriticalSize");

		Integer initialBorder = config.getObject("uhc.border.InitialSize", Integer.class);
		if(initialBorder == null)
			throw new IllegalStateException("Cannot find uhc.border.InitialSize");

		List<Map<?, ?>> changes = config.getMapList("uhc.border.changes");
		if(changes.size() == 0)
			throw new IllegalStateException("Couldn't find uhc.border.changes");

		for(Map<?, ?> change : changes) {

			String sizeStr, timestampStr, shrinktimeStr;
			int size, timestamp, shrinkTime;

			sizeStr = change.get("Size")
					.toString();
			timestampStr = change.get("Timestamp")
					.toString();
			shrinktimeStr = change.get("ShrinkTime")
					.toString();

			try {
				size = Integer.parseInt(sizeStr);
				timestamp = Integer.parseInt(timestampStr);
				shrinkTime = Integer.parseInt(shrinktimeStr);
			} catch(NumberFormatException e) {
				throw new IllegalStateException("Error while parsing a String to an it in uhc.border.changes", e);
			}

			borders.put(timestamp, new int[] { size, shrinkTime });
		}

	}

	@Override
	public boolean isFrozen() {
		return frozen;
	}

	@Override
	public @NotNull Map<String, String> dump() {

		LinkedHashMap<String, String> map = new LinkedHashMap<>();

		map.put("WorldName", String.valueOf(world == null ? null : world.getName()));
		map.put("GamerCount", String.valueOf(gamers.size()));
		map.put("BondCount", String.valueOf(gamers.size()));
		map.put("Frozen", String.valueOf(frozen));
		map.put("Running", String.valueOf(runTask != null && !runTask.isCancelled()));
		map.put("Minutes", String.valueOf(minutes));

		return map;
	}

	@Override
	public @NotNull Scoreboard getScoreboard() {
		return scoreboard;
	}

	@EventHandler
	void onPlayerQuit(PlayerQuitEvent event) {

		Player player = event.getPlayer();
		GamerImpl gamer = gamers.get(player.getUniqueId());

		if(gamer != null) {

			// TODO: ON_PLAYER_QUIT DURING GAME

		}
	}

	@EventHandler
	void onPlayerJoin(PlayerJoinEvent event) {

		Player player = event.getPlayer();
		GamerImpl gamer = gamers.get(player.getUniqueId());

		if(gamer != null) {
			gamer.updateReference(player);

			// TODO: ON_PLAYER_JOIN DURING GAME

		}
	}

	@EventHandler
	void onPlayerDeath(PlayerDeathEvent event) {

		Player player = event.getEntity();
		GamerImpl gamer = getGamer(player);

		// TODO: GET DEATH REASON

		if(gamer != null && gamer.isAlive() && gamer.hasBond()) {

			gamer.setAlive(false);
			BondImpl bond = gamer.getBond();

			//noinspection ConstantConditions
			broadcast("Gamer " + bond.getColor() + player.getDisplayName() + ChatColor.RESET + " has died!");

			Location location = player.getLocation();
			Bukkit.getScheduler()
					.runTaskLater(UHC.plugin(), () -> {
						//noinspection ConstantConditions
						player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
								.setBaseValue(20);
						player.setHealth(20);
						player.teleport(location);
						player.setGameMode(GameMode.SPECTATOR);
					}, 1);

			boolean bondDead = bond.getGamers()
					.stream()
					.noneMatch(Gamer::isAlive);

			if(bondDead) {

				broadcast("Bond " + bond.getColor() + bond.getName() + ChatColor.RESET + " has been broken!");

				List<BondImpl> bondsLeft = bonds.values()
						.stream()
						.filter(bond1 -> bond1.getGamers()
								.stream()
								.anyMatch(GamerImpl::isAlive))
						.collect(Collectors.toList());

				if(bondsLeft.size() == 1) {

					Bond winnerBond = bondsLeft.get(0);

					broadcast("There only is one bond remaining. Bond " + winnerBond.getColor() + winnerBond.getName() + ChatColor.RESET + " wins!");
					stop();

				}

			}

		}

	}

}
