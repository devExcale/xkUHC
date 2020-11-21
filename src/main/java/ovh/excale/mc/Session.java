package ovh.excale.mc;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.utils.PlayerSpreadder;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Session implements Listener {

	private static final Map<Challenger, Session> sessionMap = Collections.synchronizedMap(new HashMap<>());

	public static @Nullable Session by(Player player) {
		return by(Challenger.of(player));
	}

	public static @Nullable Session by(Challenger challenger) {
		return sessionMap.get(challenger);
	}

	public static @NotNull Session create(Player player) {
		return create(Challenger.of(player));
	}

	public static @NotNull Session create(Challenger challenger) {
		Session session = new Session(challenger);

		Session oldSession = sessionMap.put(challenger, session);
		if(oldSession != null)
			oldSession.purge();

		return session;
	}

	private final Challenger mod;
	private TeamManager teamManager;
	private final Set<Challenger> players;
	private final boolean debug;

	private final Scoreboard scoreboard;

	private World world;
	private String worldId;
	private WorldBorder worldBorder;
	private BukkitTask task;
	private int worldSize;
	private int minutes;

	// FIXME: SCOREBOARD DOESN'T SHOW
	private Session(@NotNull Challenger mod) {
		this.mod = Objects.requireNonNull(mod);
		//noinspection ConstantConditions
		scoreboard = Bukkit.getScoreboardManager()
				.getNewScoreboard();

		players = new HashSet<>();
//		teamManager = new TeamManager(scoreboard);
		scoreboard.registerNewObjective("tab_hearts", "health", "Health", RenderType.HEARTS)
				.setDisplaySlot(DisplaySlot.PLAYER_LIST);

		debug = UHC.DEBUG_MODE;
		minutes = 0;
		worldId = "0";
	}

	public TeamManager getTeamManager() {
		return teamManager;
	}

	public Optional<World> generateWorld() {
		String millis = String.valueOf(System.currentTimeMillis());

		Optional<World> optional = new WorldManager(millis + ".xkuhc").generate();
		optional.ifPresent(world -> {
			this.world = world;
			worldId = millis;

			worldSize = !debug ? 2000 : 400;
			worldBorder = world.getWorldBorder();
			worldBorder.setCenter(0, 0);
			worldBorder.setSize(worldSize);
		});

		return optional;
	}

	public void start() {

		if(world == null) {
			mod.vanilla()
					.sendMessage("World not generated, generate a world first.");
			return;
		}

//		for(UhcTeam team : teamManager.getTeams())
//			players.addAll(team.challengers());

		Bukkit.getScheduler()
				.runTask(UHC.plugin(), () -> {

					PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 2400, 100, false, false, false);
					PotionEffect regeneration = new PotionEffect(PotionEffectType.REGENERATION, 2400, 100, false, false, false);
					PotionEffect saturation = new PotionEffect(PotionEffectType.SATURATION, 2400, 100, false, false, false);
					PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 2400, 100, false, false, false);

					players.forEach(challenger -> {
//						challenger.setScoreboard(scoreboard);
						Player player = challenger.vanilla();
						player.addScoreboardTag(worldId);

						player.setGameMode(GameMode.SURVIVAL);
						player.setFoodLevel(20);
						//noinspection ConstantConditions
						player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
								.setBaseValue(40);
						player.setHealth(40);
						player.setLevel(0);
						player.getInventory()
								.clear();

						player.addPotionEffect(resistance);
						player.addPotionEffect(regeneration);
						player.addPotionEffect(saturation);
						player.addPotionEffect(blindness);
					});
				});

		Bukkit.getServer()
				.dispatchCommand(Bukkit.getConsoleSender(), "advancement revoke @a[tag=" + worldId + "] everything");
		PlayerSpreadder spreadder = new PlayerSpreadder(world, worldSize);
		teamManager.getTeams()
				.forEach(team -> spreadder.spread(team.members()
						.toArray(new Player[0])));

		AtomicInteger seconds = new AtomicInteger(5);
		AtomicReference<BukkitTask> taskReference = new AtomicReference<>();

		taskReference.set(Bukkit.getScheduler()
				.runTaskTimerAsynchronously(UHC.plugin(), () -> {

					if(seconds.get() > 0)
						broadcast("UHC - " + seconds);

					else {
						broadcast("UHC - Start!");
						BukkitScheduler scheduler = Bukkit.getScheduler();

						scheduler.runTask(UHC.plugin(), () -> {
							for(Challenger challenger : players) {
								Player player = challenger.vanilla();

								for(PotionEffect potionEffect : player.getActivePotionEffects())
									player.removePotionEffect(potionEffect.getType());
							}
						});

//						Challenger.DisconnectListener.getInstance()
//								.start();
						Bukkit.getPluginManager()
								.registerEvents(this, UHC.plugin());

						taskReference.get()
								.cancel();
						task = scheduler.runTaskTimerAsynchronously(UHC.plugin(), !debug ? this::run : this::debugRun, 100, 1200);
					}

					seconds.getAndDecrement();
				}, 0, 20));
	}

	public void stop() {

		task.cancel();

	}

	private void run() {

		switch(minutes) {

			case 7:
				worldBorder.setSize(1400, 600);
				broadcast("The border is shrinking from 2000 blocks to 1400!\nYou have 10 minutes to get to the center.");
				break;

			case 17:
			case 31:
			case 51:
				broadcast("The border has stopped shrinking.");
				break;

			case 24:
				worldBorder.setSize(800, 600);
				broadcast("The border is shrinking from 1400 blocks to 800!\nYou have 10 minutes to get to the center.");
				break;

			case 41:
				worldBorder.setSize(400, 600);
				broadcast("The border is shrinking from 800 blocks to 400!\nYou have 10 minuts to get to the center.");
				break;

			case 58:
				worldBorder.setSize(50, 600);
				broadcast("The border is shrinking from 400 blocks to 50!\nYou have 10 minutes to get to the center.");
				break;

			case 68:
				broadcast("The border has stopped shrinking. It will resume shrinking to 1 block in 5 minutes.");
				break;

			case 73:
				worldBorder.setSize(1, 600);
				broadcast("The border has resumed shrinking to its minimum. Deadmatch time!");
				break;

		}

		minutes++;
	}

	private void debugRun() {

		switch(minutes) {

			case 5:
				worldBorder.setSize(50, 600);
				broadcast("400 -> 50 in 10 minutes");
				break;

			case 15:
			case 25:
				broadcast("Border stopped shrinking");
				break;

			case 20:
				worldBorder.setSize(1, 300);
				broadcast("50 -> 1 in 5 minutes");
				break;

			case 30:
				broadcast("end");
				task.cancel();
				break;

		}

		minutes++;
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();

		boolean isChallenger = players.stream()
				.filter(Challenger::isAlive)
				.anyMatch(challenger -> challenger.is(player));

		if(isChallenger) {

			Challenger challenger = Challenger.of(player);
			Team team = challenger.getTeam();

			broadcast("Challenger " + player.getDisplayName() + " has died!");
			Bukkit.getScheduler()
					.runTaskLater(UHC.plugin(), () -> {
						AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
						//noinspection ConstantConditions
						attribute.setBaseValue(attribute.getDefaultValue());
						player.setHealth(20);
						player.setFoodLevel(20);
						player.setGameMode(GameMode.SPECTATOR);
						player.teleport(world.getSpawnLocation());
					}, 1);

			challenger.setAlive(false);
			if(team != null && team.isEliminated())
				broadcast("UhcTeam " + team.getName() + " has been eliminated!");

			int teamsLeft = (int) teamManager.getTeams()
					.stream()
					.filter(ovh.excale.mc.api.Team::isAlive)
					.count();

			if(teamsLeft == 1) {
				broadcast("Game ended!");
				worldBorder.setSize(worldSize);
				PlayerDeathEvent.getHandlerList()
						.unregister(this);
				task.cancel();
			}
		}

	}

	public void purge() {

		if(task != null)
			if(!task.isCancelled())
				task.cancel();

		PlayerDeathEvent.getHandlerList()
				.unregister(this);

		if(world != null) {
			World defWorld = Bukkit.getWorlds()
					.get(0);
			world.getPlayers()
					.forEach(player -> player.teleport(defWorld.getSpawnLocation()));

			Bukkit.getServer()
					.unloadWorld(world, false);
		}

		for(Challenger challenger : players) {
			Player player = challenger.vanilla();
			player.getInventory()
					.clear();
			try {
				//noinspection ConstantConditions
				player.setScoreboard(Bukkit.getScoreboardManager()
						.getMainScoreboard());

				AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
				//noinspection ConstantConditions
				attribute.setBaseValue(attribute.getDefaultValue());
			} catch(Exception ignored) {
			}
		}

		sessionMap.remove(mod);
	}

	public void broadcast(String message) {
		for(Challenger challenger : players)
			challenger.vanilla()
					.sendMessage(message);
	}

	public String debug() {

		String worldName = (world != null) ? world.getName() : "undefined-world";
		String mod = this.mod.vanilla()
				.getDisplayName();
		String taskId = (task != null) ? String.valueOf(task.getTaskId()) : "-1";
		String running = (task != null) ? (task.isCancelled() ? "stopped" : "running") : "stopped";

		String s = "[" + worldName + "]\n Mod: " + mod + "\n WorldId: " + worldId + "\n Task: " + taskId + " " + running + "\n Debug: " + debug + "\n Minutes: " + minutes + "\n Players: ";
		String[] names = players.stream()
				.map(challenger -> challenger.vanilla()
						.getDisplayName())
				.toArray(String[]::new);
		s += Arrays.toString(names);

		return s;
	}

}
