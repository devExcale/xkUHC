package ovh.excale.xkuhc.eventhandlers;

import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import ovh.excale.xkuhc.core.Game;
import ovh.excale.xkuhc.core.Game.Phase;
import ovh.excale.xkuhc.core.Gamer;
import ovh.excale.xkuhc.core.GamerHub;
import ovh.excale.xkuhc.effects.CaveRepellentEffect;
import ovh.excale.xkuhc.xkUHC;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static net.md_5.bungee.api.ChatColor.GOLD;
import static net.md_5.bungee.api.ChatColor.GRAY;
import static net.md_5.bungee.api.ChatMessageType.ACTION_BAR;
import static org.bukkit.Material.GHAST_TEAR;
import static org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
import static org.bukkit.event.block.Action.RIGHT_CLICK_AIR;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
import static org.bukkit.event.entity.EntityTargetEvent.TargetReason.CLOSEST_PLAYER;

public class MobRepellentHandler extends PlayerInteractionHandler {

	private final Game game;
	private final GamerHub hub;

	private BukkitTask clock;

	public MobRepellentHandler(Game game) {
		super();

		this.game = game;
		hub = game.getHub();

		clock = null;

	}

	@Override
	public void enable() {

		if(isEnabled())
			return;

		super.enable();

		Bukkit.getScheduler()
				.callSyncMethod(xkUHC.instance(), () -> Bukkit.addRecipe(CaveRepellentEffect.recipe()));

		clock = Bukkit.getScheduler()
				.runTaskTimerAsynchronously(xkUHC.instance(), this::updateEffects, 0, 5);

	}

	@Override
	public void disable() {

		if(!isEnabled())
			return;

		super.disable();

		Bukkit.getScheduler()
				.callSyncMethod(xkUHC.instance(), () -> Bukkit.removeRecipe(CaveRepellentEffect.key()));

		if(clock != null && !clock.isCancelled())
			clock.cancel();

	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled() || (clock != null && !clock.isCancelled());
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

	@Override
	public void onPhaseChange(@NotNull Phase phase) {

		switch(phase) {

			case RUNNING -> enable();

			case STOPPED -> disable();

		}

	}

	private void updateEffects() {

		Instant now = Instant.now();

		for(Gamer gamer : hub.getGamers()) {

			CaveRepellentEffect effect = gamer.getCustomEffect();
			if(effect == null)
				continue;

			Instant endTime = effect.getStartTime()
					.plusMillis(effect.getDuration() * 50);

			if(endTime.isBefore(now)) {

				gamer.removeCustomEffect();
				gamer.getPlayer()
						.spigot()
						.sendMessage(ACTION_BAR, new ComponentBuilder(msg.game("repellent.end")).color(GOLD)
								.create());

			} else {

				long remaining = endTime.getEpochSecond() - now.getEpochSecond();
				gamer.getPlayer()
						.spigot()
						// TODO: move message in msgbundles
						.sendMessage(ACTION_BAR, new ComponentBuilder("[Cave Repellent] Remaining %dm:%ds".formatted(remaining / 60, remaining % 60)).color(GOLD)
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

		if(gamer == null)
			return;

		item.setAmount(item.getAmount() - 1);
		player.playSound(player, ENTITY_EXPERIENCE_ORB_PICKUP, 100, 0);

		CaveRepellentEffect effect = gamer.getCustomEffect();

		if(effect == null) {

			gamer.setCustomEffect(new CaveRepellentEffect(6000L));
			player.spigot()
					.sendMessage(new ComponentBuilder(msg.game("repellent.apply")).color(GRAY)
							.italic(true)
							.create());

		} else {

			effect.addDuration(6000L);
			player.spigot()
					.sendMessage(new ComponentBuilder(msg.game("repellent.reapply")).color(GRAY)
							.italic(true)
							.create());

		}

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityTarget(EntityTargetLivingEntityEvent event) {

		if(!game.getPhase()
				.isRunning())
			return;

		if(!(event.getTarget() instanceof Player target))
			return;

		if(!CLOSEST_PLAYER.equals(event.getReason()))
			return;

		Gamer gamer = hub.getGamer(target.getUniqueId());

		if(gamer != null && gamer.getCustomEffect() != null) {

			Location loc = target.getLocation();

			int highestBlock = target.getWorld()
					.getHighestBlockYAt(loc);

			if(highestBlock - loc.getY() > 16)
				event.setCancelled(true);

		}

	}

}
