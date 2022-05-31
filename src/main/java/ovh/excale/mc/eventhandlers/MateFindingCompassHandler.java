package ovh.excale.mc.eventhandlers;

import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ovh.excale.mc.UHC;
import ovh.excale.mc.uhc.core.Bond;
import ovh.excale.mc.uhc.core.Gamer;
import ovh.excale.mc.uhc.core.GamerHub;
import ovh.excale.mc.utils.MessageBundles;

import static net.md_5.bungee.api.ChatColor.GOLD;
import static net.md_5.bungee.api.ChatMessageType.ACTION_BAR;
import static org.bukkit.Material.COMPASS;
import static org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
import static org.bukkit.event.block.Action.RIGHT_CLICK_AIR;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

public class MateFindingCompassHandler implements Listener {

	private static final Sound USE_SOUND = ENTITY_EXPERIENCE_ORB_PICKUP;

	private final GamerHub hub;
	private final MessageBundles msg;

	public MateFindingCompassHandler(GamerHub hub) {

		this.hub = hub;
		msg = UHC.instance()
				.getMessages();

	}

	public void activate() {

		Bukkit.getPluginManager()
				.registerEvents(this, UHC.instance());

	}

	public void deactivate() {

		PlayerInteractEvent.getHandlerList()
				.unregister(this);

	}

	@EventHandler(priority = EventPriority.HIGH)
	private void onCompassInteract(PlayerInteractEvent event) {

		Action action = event.getAction();

		if(!RIGHT_CLICK_AIR.equals(action) && !RIGHT_CLICK_BLOCK.equals(action))
			return;

		ItemStack item = event.getItem();

		if(item == null || !COMPASS.equals(item.getType()))
			return;

		Player player = event.getPlayer();
		Gamer gamer = hub.getGamer(player.getUniqueId());

		if(gamer == null)
			return;

		player.playSound(player.getLocation(), USE_SOUND, 100, 0);

		Bond bond = gamer.getBond();

		if(bond == null)
			return;

		Gamer target;
		String targetName;

		if(player.isSneaking()) {

			Gamer[] teammates = bond.getGamers()
					.stream()
					.filter(Gamer::isAlive)
					.filter(gamer1 -> !gamer1.getUniqueId()
							.equals(gamer.getUniqueId()))
					.toArray(Gamer[]::new);

			if(teammates.length == 0) {

				player.spigot()
						.sendMessage(ACTION_BAR, new ComponentBuilder(msg.game("compass.no_teammates")).color(GOLD)
								.create());
				return;
			}

			target = teammates[(int) (teammates.length * Math.random())];
			targetName = target.getPlayer()
					.getName();

			gamer.setCompassTracking(target);

			player.spigot()
					.sendMessage(ACTION_BAR, new ComponentBuilder(msg.game("compass.now_tracking", targetName)).color(GOLD)
							.create());

		} else {

			target = gamer.getCompassTracking();

			if(target == null || !target.isAlive()) {

				gamer.setCompassTracking(null);
				player.spigot()
						.sendMessage(ACTION_BAR, new ComponentBuilder(msg.game("compass.no_tracking")).color(GOLD)
								.create());

				return;
			}

			gamer.updateCompassTracking();
			targetName = target.getPlayer()
					.getName();

			player.spigot()
					.sendMessage(ACTION_BAR, new ComponentBuilder(msg.game("compass.update_tracking", targetName)).color(GOLD)
							.create());

		}

	}

}
