package ovh.excale.mc.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.BiomeProvider;
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

		long millis = System.currentTimeMillis();
		String name = "%x.xkuhc".formatted(millis);

		player.sendMessage("Starting generation");

		WorldCreator c = new WorldCreator(name).seed(millis);
		World w = c.createWorld();
		BiomeProvider prov = w.getBiomeProvider();
		System.out.println(prov);

		WorldManager wGen = new WorldManager().loadSpawn(false)
				.generateOnce()
				.applyRules();

		World world = wGen.getWorld();
		player.sendMessage("World generated, commencing teleport");

		PlayerSpreader spreader = new PlayerSpreader(world, 1000);
		spreader.spread(player);

		player.sendMessage("Teleported");

	}

}
