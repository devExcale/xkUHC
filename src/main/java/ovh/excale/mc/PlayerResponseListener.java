package ovh.excale.mc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class PlayerResponseListener implements Listener {

	private final Map<Player, Queue<BiConsumer<Player, String>>> queueMap;
	private final Plugin plugin;
	private final int timeout;

	public PlayerResponseListener(Plugin plugin, int timeout) {
		this.plugin = plugin;
		this.timeout = timeout;
		queueMap = Collections.synchronizedMap(new HashMap<>());
	}

	public void await(Player player, BiConsumer<Player, String> then) {
		Queue<BiConsumer<Player, String>> queue = queueMap.computeIfAbsent(player, k -> new LinkedList<>());
		AtomicReference<BukkitTask> timeoutTaskReference = new AtomicReference<>();
		BiConsumer<Player, String> consumer = then.andThen((player1, s) -> timeoutTaskReference.get()
				.cancel());
		queue.offer(consumer);

		timeoutTaskReference.set(Bukkit.getScheduler()
				.runTaskLater(plugin, () -> {
					if(queue.contains(consumer)) {
						queue.remove(consumer);
						player.sendMessage("Operation timed out.");
					}
				}, timeout * 20));
	}

	@EventHandler(priority = EventPriority.HIGH)
	private void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		Queue<BiConsumer<Player, String>> queue = queueMap.get(player);

		if(queue != null) {
			BiConsumer<Player, String> consumer = queue.poll();
			if(consumer != null) {
				Bukkit.getScheduler()
						.runTask(plugin, () -> consumer.accept(player, event.getMessage()));
				event.setCancelled(true);
			}

		}
	}

}
