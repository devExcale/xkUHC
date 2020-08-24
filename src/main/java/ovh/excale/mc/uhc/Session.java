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
import ovh.excale.mc.UHC;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Session implements Listener {

	private static final Map<Challenger, Session> sessionMap = Collections.synchronizedMap(new HashMap<>());

	public static Session by(Player player) {
		return by(Challenger.of(player));
	}

	public static Session by(Challenger challenger) {
		Session session = sessionMap.get(challenger);
		if(session == null)
			session = new Session(challenger);
		sessionMap.put(challenger, session);
		return session;
	}

	private Challenger parent;
	private World world;
	private BukkitTask task;
	private Set<Challenger> players;
	private int minutes;

	private Session(Challenger challenger) {
		parent = challenger;
		players = Collections.synchronizedSet(new HashSet<>());
	}

	public Optional<World> generateWorld() {
		Optional<World> optional = new WorldManager(System.currentTimeMillis() + ".xkuhc").generate();
		optional.ifPresent(world -> {
			this.world = world;

			WorldBorder border = world.getWorldBorder();
			border.setCenter(0, 0);
			border.setSize(2000);
		});

		return optional;
	}

	public void start() {
		Team.getAll()
				.stream()
				.flatMap(team -> team.players()
						.stream())
				.forEach(player -> {
					players.add(Challenger.of(player));
					player.setGameMode(GameMode.SURVIVAL);
					player.setFoodLevel(20);
					player.setHealth(20);
					player.setLevel(0);
					player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 5, false, false, false));
				}));

		ConsoleCommandSender console = Bukkit.getConsoleSender();
		Bukkit.getServer()
				.dispatchCommand(console, "advancement revoke @a everything");
		Bukkit.getServer()
				.dispatchCommand(console, "spreadplayers 0 0 150 1000 true @a");

		AtomicInteger seconds = new AtomicInteger(5);
		AtomicReference<BukkitTask> taskReference = new AtomicReference<>();

		taskReference.set(Bukkit.getScheduler()
				.runTaskTimerAsynchronously(UHC.plugin(), () -> {

					if(seconds.get() > 0)
						broadcast(seconds + " left...");

					else {
						broadcast("UHC start!");

						BukkitScheduler scheduler = Bukkit.getScheduler();
						taskReference.get()
								.cancel();
						scheduler.runTaskTimerAsynchronously(UHC.plugin(), this::run, 100, 20);
					}

					seconds.getAndDecrement();
				}, 0, 20));
	}

	public void run() {

		switch(minutes--) {

			case

		}

	}

	public void broadcast(String message) {
		for(Challenger challenger : players)
			challenger.vanilla()
					.sendMessage(message);
	}

}
