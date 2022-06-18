package ovh.excale.xkuhc.eventhandlers;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.core.Game.Phase;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

public class BedInteractionHandler extends PlayerInteractionHandler {

	@Override
	public @NotNull Set<Action> enabledActionTypes() {
		return Set.of(RIGHT_CLICK_BLOCK);
	}

	@Override
	public @NotNull Set<Material> enabledMaterials() {
		return Collections.emptySet();
	}

	@Override
	public @NotNull Set<Block> enabledBlocks() {
		return Collections.emptySet();
	}

	@Override
	public void onPhaseChange(@NotNull Phase phase) {

		switch(phase) {

			case STARTING -> enable();

			case STOPPED -> disable();

		}

	}

	@EventHandler(priority = EventPriority.HIGH)
	@Override
	public void onInteract(PlayerInteractEvent event) {

		if(!accepts(event))
			return;

		boolean isBed = Optional.ofNullable(event.getClickedBlock())
				.map(Block::getType)
				.map(Material::getKey)
				.map(NamespacedKey::getKey)
				.map(String::toLowerCase)
				.map(s -> s.endsWith("bed"))
				.orElse(false);

		if(!isBed)
			return;

		event.setCancelled(true);

		event.getPlayer()
				.spigot()
				.sendMessage(new ComponentBuilder(msg.game("game.cant_sleep")).color(ChatColor.GRAY)
						.italic(true)
						.create());

	}

}
