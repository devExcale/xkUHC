package ovh.excale.xkuhc.eventhandlers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.comms.MessageBundles;
import ovh.excale.xkuhc.core.GameAccessory;
import ovh.excale.xkuhc.xkUHC;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

public abstract class PlayerInteractionHandler implements Listener, GameAccessory {

	protected final xkUHC instance;
	protected final MessageBundles msg;
	protected final Logger log;

	private boolean enabled;

	public PlayerInteractionHandler() {

		instance = xkUHC.instance();
		msg = instance.getMessages();
		log = instance.getLogger();

		enabled = false;

	}

	@Override
	public void enable() {

		Bukkit.getPluginManager()
				.registerEvents(this, instance);

		enabled = true;

	}

	@Override
	public void disable() {

		PlayerInteractEvent.getHandlerList()
				.unregister(this);

		enabled = false;

	}

	@Override
	public boolean isEnabled() {
		return enabled;
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
