package ovh.excale.mc.eventhandlers;

import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import ovh.excale.mc.uhc.core.Bond;
import ovh.excale.mc.uhc.core.Gamer;
import ovh.excale.mc.uhc.core.GamerHub;

import java.util.Collections;
import java.util.Set;

import static net.md_5.bungee.api.ChatColor.GOLD;
import static net.md_5.bungee.api.ChatMessageType.ACTION_BAR;
import static org.bukkit.Material.COMPASS;
import static org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
import static org.bukkit.event.block.Action.RIGHT_CLICK_AIR;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

public class MateFindingCompassHandler extends PlayerInteractionHandler {

	private static final Sound USE_SOUND = ENTITY_EXPERIENCE_ORB_PICKUP;

	private static final Set<Action> ENABLED_ACTIONS;
	private static final Set<Material> ENABLED_MATERIALS;

	static {
		ENABLED_ACTIONS = Set.of(RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK);
		ENABLED_MATERIALS = Set.of(COMPASS);
	}

	private final GamerHub hub;

	public MateFindingCompassHandler(GamerHub hub) {
		super();

		this.hub = hub;

	}

	@Override
	public @NotNull Set<Action> enabledActionTypes() {
		return ENABLED_ACTIONS;
	}

	@Override
	public @NotNull Set<Material> enabledMaterials() {
		return ENABLED_MATERIALS;
	}

	@Override
	public @NotNull Set<Block> enabledBlocks() {
		return Collections.emptySet();
	}

	@EventHandler(priority = EventPriority.HIGH)
	@Override
	public void onInteract(PlayerInteractEvent event) {

		if(!accepts(event))
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
