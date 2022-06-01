package ovh.excale.mc.eventhandlers;

import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ovh.excale.mc.UHC;
import ovh.excale.mc.effects.CaveRepellentEffect;
import ovh.excale.mc.uhc.Game;
import ovh.excale.mc.uhc.core.Gamer;
import ovh.excale.mc.uhc.core.GamerHub;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static net.md_5.bungee.api.ChatColor.GOLD;
import static net.md_5.bungee.api.ChatMessageType.ACTION_BAR;
import static org.bukkit.Material.GHAST_TEAR;
import static org.bukkit.event.block.Action.RIGHT_CLICK_AIR;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
import static ovh.excale.mc.uhc.Game.Status.RUNNING;

public class MobRepellentHandler extends PlayerInteractionHandler {

	private final Game game;
	private final GamerHub hub;

	private BukkitTask clock;

	public MobRepellentHandler(Game game) {
		super();

		this.game = game;
		hub = game.getHub();

	}

	@Override
	public void activate() {
		super.activate();

		Bukkit.addRecipe(CaveRepellentEffect.recipe());

		clock = Bukkit.getScheduler()
				.runTaskTimerAsynchronously(UHC.instance(), this::updateEffects, 0, 5);

	}

	@Override
	public void deactivate() {
		super.deactivate();

		Bukkit.removeRecipe(CaveRepellentEffect.key());

		clock.cancel();

	}

	@Override
	public @NotNull Set<Action> enabledActionTypes() {
		return Set.of(RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK);
	}

	@Override
	public @NotNull Set<Material> enabledMaterials() {
		return Set.of(GHAST_TEAR);
	}

	@Override
	public @NotNull Set<Block> enabledBlocks() {
		return Collections.emptySet();
	}

	private void updateEffects() {

		Instant now = Instant.now();

		for(Gamer gamer : hub.getGamers()) {

			CaveRepellentEffect effect = gamer.getCustomEffect();
			if(effect == null)
				continue;

			Instant endTime = effect.getStartTime()
					.plusMillis(effect.getDuration() * 50);

			if(endTime.isAfter(now)) {

				gamer.removeCustomEffect();
				gamer.getPlayer()
						.spigot()
						.sendMessage(ACTION_BAR, new ComponentBuilder(msg.game("repellent.end")).color(GOLD)
								.create());

			} else {

				long remaining = endTime.getEpochSecond() - now.getEpochSecond();
				gamer.getPlayer()
						.spigot()
						.sendMessage(ACTION_BAR, new ComponentBuilder(
								"[Cave Repellent] Remaining %dm:%ds".formatted(remaining / 60, remaining % 60)).color(
										GOLD)
								.create());
			}

		}

	}

	@EventHandler(priority = EventPriority.HIGH)
	@Override
	public void onInteract(PlayerInteractEvent event) {

		if(!accepts(event))
			return;

		ItemStack item = Objects.requireNonNull(event.getItem());
		ItemMeta itemMeta = Objects.requireNonNull(item.getItemMeta());

		if(!itemMeta.hasLore())
			return;

		Player player = event.getPlayer();
		Gamer gamer = hub.getGamer(player.getUniqueId());

		// TODO

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityTarget(EntityTargetLivingEntityEvent event) {

		if(!RUNNING.equals(game.getStatus()))
			return;

		if(!(event.getTarget() instanceof Player target))
			return;

		Gamer gamer = hub.getGamer(target.getUniqueId());

		if(gamer == null)
			return;

		// TODO

	}

}
