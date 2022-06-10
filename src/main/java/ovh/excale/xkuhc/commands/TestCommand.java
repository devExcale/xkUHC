package ovh.excale.xkuhc.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AFloatArgument;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import dev.jorel.commandapi.annotations.arguments.ASoundArgument;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ovh.excale.xkuhc.xkUHC;
import ovh.excale.xkuhc.world.WorldManager;
import ovh.excale.xkuhc.world.PlayerSpreader;

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

		PlayerSpreader spreader = new PlayerSpreader(world, 1000);
		spreader.spread(player);

		player.sendMessage("Teleported");

	}

	@Subcommand("jump")
	public static void jump(Player player) {

		Bukkit.getScheduler()
				.runTaskAsynchronously(xkUHC.instance(), () -> {

					World world = new WorldManager().loadSpawn(false)
							.generateUntilClearCenter()
							.applyRules()
							.getWorld();

					new PlayerSpreader(world, 2000).spread(player);

				});

	}

	@Subcommand("sound")
	public static void sound(Player player, @ASoundArgument Sound sound, @AFloatArgument float pitch, @AFloatArgument float volume) {
		player.playSound(player, sound, volume, pitch);
	}

	@Subcommand("sound")
	public static void soundNoVolume(Player player, @ASoundArgument Sound sound, @AFloatArgument float pitch) {
		sound(player, sound, pitch, 100f);
	}

}
