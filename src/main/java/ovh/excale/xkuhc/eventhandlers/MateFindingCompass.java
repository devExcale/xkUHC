package ovh.excale.xkuhc.eventhandlers;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.core.Bond;
import ovh.excale.xkuhc.core.Gamer;
import ovh.excale.xkuhc.core.GamerHub;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static net.md_5.bungee.api.ChatColor.GOLD;
import static net.md_5.bungee.api.ChatColor.ITALIC;
import static org.bukkit.Material.COMPASS;
import static org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
import static org.bukkit.event.block.Action.RIGHT_CLICK_AIR;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

public class MateFindingCompass extends PlayerInteractionHandler {

	private static final Sound USE_SOUND = ENTITY_EXPERIENCE_ORB_PICKUP;

	private static final Set<Action> ENABLED_ACTIONS;
	private static final Set<Material> ENABLED_MATERIALS;

	static {
		ENABLED_ACTIONS = Set.of(RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK);
		ENABLED_MATERIALS = Set.of(COMPASS);
	}

	private final GamerHub hub;

	public MateFindingCompass(GamerHub hub) {
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

		ItemStack item = Objects.requireNonNull(event.getItem());
		ItemMeta itemMeta = Objects.requireNonNull(item.getItemMeta());
		String itemName = GOLD.toString() + ITALIC;

		if(player.isSneaking()) {

			Gamer[] teammates = bond.getGamers()
					.stream()
					.filter(Gamer::isAlive)
					.filter(gamer1 -> !gamer1.getUniqueId()
							.equals(gamer.getUniqueId()))
					.toArray(Gamer[]::new);

			if(teammates.length != 0) {

				target = teammates[(int) (teammates.length * Math.random())];
				targetName = target.getPlayer()
						.getName();

				gamer.setCompassTracking(target);

				itemName += targetName;

			} else
				itemName += msg.game("compass.no_teammates");

		} else {

			target = gamer.getCompassTracking();

			if(target == null || !target.isAlive()) {

				gamer.setCompassTracking(null);
				itemName += msg.game("compass.no_tracking");

			} else {

				gamer.updateCompassTracking();
				targetName = target.getPlayer()
						.getName();

				itemName += targetName;

			}

		}

		itemMeta.setDisplayName(itemName);
		itemMeta.setLore(List.of(msg.game("compass.tooltip")));

		item.setItemMeta(itemMeta);

		PlayerInventory playerInv = player.getInventory();
		playerInv.setItemInMainHand(item);

	}

}
