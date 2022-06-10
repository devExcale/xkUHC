package ovh.excale.xkuhc.eventhandlers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.xkUHC;
import ovh.excale.xkuhc.comms.MessageBundles;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

public abstract class PlayerInteractionHandler implements Listener {

	protected final MessageBundles msg;
	protected final Logger log;

	public PlayerInteractionHandler() {

		msg = xkUHC.instance()
				.getMessages();

		log = xkUHC.log();

	}

	public void activate() {

		Bukkit.getPluginManager()
				.registerEvents(this, xkUHC.instance());

	}

	public void deactivate() {

		PlayerInteractEvent.getHandlerList()
				.unregister(this);

	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean accepts(PlayerInteractEvent event) {

		boolean action = enabledActionTypes().contains(event.getAction());

		Set<Material> enabledMaterials = enabledMaterials();
		boolean material = enabledMaterials.isEmpty() || Optional.ofNullable(event.getItem())
				.map(ItemStack::getType)
				.map(enabledMaterials::contains)
				.orElse(false);

		Set<Block> enabledBlocks = enabledBlocks();
		boolean block = enabledBlocks.isEmpty() || Optional.ofNullable(event.getClickedBlock())
				.map(enabledBlocks::contains)
				.orElse(false);

		return action && material && block;
	}

	@NotNull
	public abstract Set<Action> enabledActionTypes();

	@NotNull
	public abstract Set<Material> enabledMaterials();

	@NotNull
	public abstract Set<Block> enabledBlocks();

	public abstract void onInteract(PlayerInteractEvent event);

}
