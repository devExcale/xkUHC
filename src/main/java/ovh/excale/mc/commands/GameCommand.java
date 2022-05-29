package ovh.excale.mc.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AIntegerArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import ovh.excale.mc.UHC;
import ovh.excale.mc.uhc.Game;
import ovh.excale.mc.uhc.core.Bond;
import ovh.excale.mc.uhc.core.Gamer;
import ovh.excale.mc.uhc.core.GamerHub;
import ovh.excale.mc.uhc.world.WorldUtils;
import ovh.excale.mc.utils.MessageBundles;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Alias("xkuhc")
@Command("uhc")
public class GameCommand {

	@Subcommand("create")
	public static void createGame(CommandSender sender) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game != null)
			throw CommandAPI.fail("There's already another session");

		try {

			UHC.setGame(new Game());

		} catch(Exception e) {
			UHC.log()
					.log(Level.SEVERE, e.getMessage(), e);
			throw CommandAPI.fail("There has been an internal error");
		}

		sender.sendMessage("Game created");

	}

	@Subcommand("start")
	public static void startGame(CommandSender sender) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game == null)
			throw CommandAPI.fail("No game found");

		try {

			game.tryStart();

		} catch(IllegalStateException e) {
			throw CommandAPI.fail(e.getMessage());
		} catch(Exception e) {
			UHC.log()
					.log(Level.SEVERE, e.getMessage(), e);
			throw CommandAPI.fail("There has been an internal error");
		}

	}

	@Subcommand("stop")
	public static void stopGame(CommandSender sender) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game == null)
			throw CommandAPI.fail("No game found");

		try {

			game.stop();
			game.getHub()
					.broadcast("Game stopped forcefully.");
			UHC.setGame(null);

		} catch(IllegalStateException e) {
			throw CommandAPI.fail(e.getMessage());
		} catch(Exception e) {
			UHC.log()
					.log(Level.SEVERE, e.getMessage(), e);
			throw CommandAPI.fail("There has been an internal error");
		}

	}

	@Subcommand("dump")
	public static void dump(CommandSender sender) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game == null)
			throw CommandAPI.fail("No game found");

		StringBuilder sb = new StringBuilder("\n [Game dump]");
		game.dump()
				.forEach((key, value) -> sb.append("\n - ")
						.append(key)
						.append(": ")
						.append(value));

		sb.append('\n');
		sender.sendMessage(sb.toString());

	}

	@Subcommand("clean")
	public static void cleanWorlds(CommandSender sender) {

		MessageBundles msg = UHC.instance()
				.getMessages();

		WorldUtils.purgeWorlds(count -> sender.sendMessage(msg.main("removed_worlds", count)));

	}

	@Subcommand("reload")
	public static void reloadConfiguration(CommandSender sender) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game != null && !game.getStatus()
				.isEditable()) {

			Plugin plugin = UHC.instance();
			plugin.reloadConfig();
			UHC.DEBUG = plugin.getConfig()
					.getBoolean("debug", false);

		} else
			throw CommandAPI.fail("Game is running");

		sender.sendMessage("Configuration reloaded.");

	}

	@Subcommand("random")
	public static void createRandomTeams(CommandSender sender, @AIntegerArgument Integer bondQty) throws WrapperCommandSyntaxException {

		if(bondQty < 2)
			throw CommandAPI.fail("Can't create less than two teams");

		Game game = UHC.getGame();

		if(game == null)
			throw CommandAPI.fail("No game found");

		// TODO: check for other bonds / game status

		GamerHub hub = game.getHub();
		List<Gamer> gamers = new LinkedList<>();

		Bukkit.getServer()
				.getOnlinePlayers()
				.forEach(player -> {

					Gamer gamer = hub.register(player);
					gamers.add(gamer);

				});

		Collections.shuffle(gamers);

		if(gamers.size() < bondQty)
			throw CommandAPI.fail("Too many teams");

		List<ChatColor> colors = Arrays.stream(ChatColor.values())
				.filter(ChatColor::isColor)
				.collect(Collectors.toList());
		Collections.shuffle(colors);
		Iterator<ChatColor> iterColors = colors.iterator();

		Bond[] bonds = IntStream.range(1, bondQty + 1)
				.mapToObj(i -> hub.createBond("Team" + i))
				.peek(bond -> hub.setBondColor(bond, iterColors.next()))
				.toArray(Bond[]::new);
		AtomicInteger iBonds = new AtomicInteger(0);

		gamers.forEach(gamer -> hub.boundGamer(bonds[iBonds.getAndIncrement() % bondQty], gamer));

	}

}
