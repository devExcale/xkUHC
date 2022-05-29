package ovh.excale.mc.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ovh.excale.mc.uhc.world.WorldManager;
import ovh.excale.mc.utils.PlayerSpreader;

@SuppressWarnings("unused")
@Command("test")
public class TestCommand {

	@Default
	public static void nothing(CommandSender commandSender) {

		commandSender.sendMessage("Nothing to see here");

	}

	@Subcommand("gen")
	public static void genWorldAndTp(Player player, @APlayerArgument Player otherPlayer) {

		player.sendMessage("Starting generation");

		World world = new WorldManager().loadSpawn(false)
				.generateUntilClearCenter()
				.applyRules()
				.getWorld();

		player.sendMessage("World generated, commencing teleport");

		PlayerSpreader spreader = new PlayerSpreader(world, 1000);

		spreader.spread(player);
		spreader.spread(otherPlayer);

		player.sendMessage("Teleported");

	}

	@Subcommand("gen")
	public static void genWorldAndTp(Player player) {

		player.sendMessage("Starting generation");

		World world = new WorldManager().loadSpawn(false)
				.generateUntilClearCenter()
				.applyRules()
				.getWorld();

		player.sendMessage("World generated, commencing teleport");

		PaperLib.teleportAsync(player, new Location(world, 100, 100, 100));

		player.sendMessage("Teleported");

	}

}