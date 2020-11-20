package ovh.excale.mc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class PlayerResponseListener implements Listener {

	private final Map<Player, Consumer<String>> consumerMap;
	private final Plugin plugin;
	private final int timeout;

	public PlayerResponseListener(Plugin plugin, int timeout) {
		this.plugin = plugin;
		this.timeout = timeout;
		consumerMap = Collections.synchronizedMap(new HashMap<>());
	}

	public void await(Player player, Consumer<String> then) {

		AtomicReference<BukkitTask> timeoutTaskReference = new AtomicReference<>();
		Consumer<String> consumer = then.andThen(s -> {
			consumerMap.remove(player);
			timeoutTaskReference.get()
					.cancel();
		});

		consumerMap.put(player, consumer);

		timeoutTaskReference.set(Bukkit.getScheduler()
				.runTaskLaterAsynchronously(plugin, () -> {

					if(consumerMap.containsKey(player)) {
						consumerMap.remove(player);
						player.sendMessage("Operation timed out.");
					}

				}, timeout * 20));
	}

	@EventHandler(priority = EventPriority.HIGH)
	private void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		Consumer<String> consumer = consumerMap.get(player);

		if(!event.isCancelled())
			if(consumer != null) {

				event.setCancelled(true);
				consumerMap.remove(player);

				Bukkit.getScheduler()
						.runTask(plugin, () -> consumer.accept(event.getMessage()));
			}
	}

}
