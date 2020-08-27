package ovh.excale.mc.uhc;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.UHC;

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
	private final Set<Challenger> players;

	private World world;
	private String worldId;
	private WorldBorder worldBorder;
	private BukkitTask task;
	private int minutes;

	private Session(@NotNull Challenger mod) {
		this.mod = Objects.requireNonNull(mod);
		players = Collections.synchronizedSet(new HashSet<>());
		worldId = "-1";
	}

	public Optional<World> generateWorld() {
		String millis = String.valueOf(System.currentTimeMillis());

		Optional<World> optional = new WorldManager(millis + ".xkuhc").generate();
		optional.ifPresent(world -> {
			this.world = world;
			worldId = millis;

			worldBorder = world.getWorldBorder();
			worldBorder.setCenter(0, 0);
			worldBorder.setSize(2000);
		});

		return optional;
	}

	public void start() {
		Team.getAll()
				.stream()
				.flatMap(team -> team.players()
						.stream())
				.forEach(player -> players.add(Challenger.of(player)));

		if(world == null) {
			mod.vanilla()
					.sendMessage("World not generated, generate a world first.");
			return;
		}

		players.forEach(challenger -> {
			Player player = challenger.vanilla();
			player.setGameMode(GameMode.SURVIVAL);
			player.setFoodLevel(20);
			player.setHealth(20);
			player.setLevel(0);
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 5, false, false, false));
			player.addScoreboardTag(worldId);
		});

		ConsoleCommandSender console = Bukkit.getConsoleSender();
		Bukkit.getServer()
				.dispatchCommand(console, "advancement revoke @a[tag=" + worldId + "] everything");
		Bukkit.getServer()
				.dispatchCommand(console, "spreadplayers 0 0 150 1000 true @a[tag=" + worldId + "]");

		AtomicInteger seconds = new AtomicInteger(5);
		AtomicReference<BukkitTask> taskReference = new AtomicReference<>();

		taskReference.set(Bukkit.getScheduler()
				.runTaskTimerAsynchronously(UHC.plugin(), () -> {

					if(seconds.get() > 0)
						broadcast("UHC - " + seconds);

					else {
						broadcast("UHC - Start!");

						BukkitScheduler scheduler = Bukkit.getScheduler();
						taskReference.get()
								.cancel();
						task = scheduler.runTaskTimerAsynchronously(UHC.plugin(), this::run, 100, 20);
					}

					seconds.getAndDecrement();
				}, 0, 20));
	}

	public void stop() {

		task.cancel();

	}

	private void run() {

		task.cancel();

	}

	public void purge() {

		if(task != null)
			if(!task.isCancelled())
				task.cancel();

		World defWorld = Bukkit.getWorlds()
				.get(0);
		world.getPlayers()
				.forEach(player -> player.teleport(defWorld.getSpawnLocation()));

		Bukkit.getServer()
				.unloadWorld(world, false);

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

		String s = "[" + worldName + "]\n Mod: " + mod + "\n WorldId: " + worldId + "\n Task: " + taskId + " " + running + "\n Minutes: " + minutes + "\n Players: ";
		String[] names = players.stream()
				.map(challenger -> challenger.vanilla()
						.getDisplayName())
				.toArray(String[]::new);
		s += Arrays.toString(names);

		return s;

	}

}
