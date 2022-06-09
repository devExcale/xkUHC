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
import ovh.excale.mc.utils.FakerWrapper;
import ovh.excale.mc.utils.MessageBundles;
import ovh.excale.mc.utils.MessageFormatter;

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

		MessageBundles msg = UHC.instance()
				.getMessages();

		Game game = UHC.getGame();
		if(game != null)
			throw CommandAPI.fail(msg.main("game.another_session"));

		try {

			UHC.setGame(new Game());

		} catch(Exception e) {
			UHC.log()
					.log(Level.SEVERE, e.getMessage(), e);
			throw CommandAPI.fail(msg.main("error.internal"));
		}

		sender.sendMessage(new MessageFormatter().addColors()
				.formatFine(msg.main("game.created")));

	}

	@Subcommand("start")
	public static void startGame(CommandSender sender) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		Game game = UHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		try {

			game.tryStart();

		} catch(IllegalStateException e) {
			throw CommandAPI.fail(e.getMessage());
		} catch(Exception e) {
			UHC.log()
					.log(Level.SEVERE, e.getMessage(), e);
			throw CommandAPI.fail(msg.main("error.internal"));
		}

	}

	@Subcommand("confirm")
	public static void confirmStart(CommandSender sender) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		Game game = UHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		game.setConfirmStart(true);

		sender.sendMessage(new MessageFormatter().formatFine(msg.main("game.confirmed")));

	}

	@Subcommand("stop")
	public static void stopGame(CommandSender sender) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		Game game = UHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		try {

			game.stop();
			game.getHub()
					.broadcast(msg.game("game.stop_force"));
			UHC.setGame(null);

		} catch(IllegalStateException e) {
			throw CommandAPI.fail(e.getMessage());
		} catch(Exception e) {
			UHC.log()
					.log(Level.SEVERE, e.getMessage(), e);
			throw CommandAPI.fail(msg.main("error.internal"));
		}

	}

	@Subcommand("dump")
	public static void dump(CommandSender sender) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		Game game = UHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

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

		WorldUtils.purgeWorlds(count -> sender.sendMessage(msg.main("misc.removed_worlds", count)));

	}

	@Subcommand("reload")
	public static void reloadConfiguration(CommandSender sender) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		Game game = UHC.getGame();

		if(game == null || !game.getStatus()
				.isEditable()) {

			Plugin plugin = UHC.instance();
			plugin.reloadConfig();
			UHC.DEBUG = plugin.getConfig()
					.getBoolean("debug", false);

		} else
			throw CommandAPI.fail(msg.main("game.running"));

		sender.sendMessage(new MessageFormatter().addColors()
				.formatFine(msg.main("config.reloaded")));

	}

	@Subcommand("random")
	public static void createRandomTeams(CommandSender sender, @AIntegerArgument Integer bondQty) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		if(bondQty < 2)
			throw CommandAPI.fail(msg.main("bond.less_two"));

		Game game = UHC.getGame();

		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		// TODO: check for other bonds / game status

		GamerHub hub = game.getHub();
		List<Gamer> gamers = new LinkedList<>();

		Set<UUID> bounded = hub.getGamers()
				.stream()
				.map(Gamer::getUniqueId)
				.collect(Collectors.toSet());

		Bukkit.getServer()
				.getOnlinePlayers()
				.stream()
				.filter(player -> !bounded.contains(player.getUniqueId()))
				.forEach(player -> {

					Gamer gamer = hub.register(player);
					gamers.add(gamer);

				});

		if(gamers.size() == 0)
			throw CommandAPI.fail(msg.main("bond.gamers_all_bound"));

		Collections.shuffle(gamers);

		if(gamers.size() < bondQty)
			throw CommandAPI.fail(msg.main("bond.too_many_bounds"));

		List<ChatColor> colors = Arrays.stream(ChatColor.values())
				.filter(ChatColor::isColor)
				.collect(Collectors.toList());
		Collections.shuffle(colors);
		Iterator<ChatColor> iterColors = colors.iterator();

		String fakerString = UHC.instance()
				.getConfig()
				.getString("faker", "esports.team");
		FakerWrapper faker;
		try {
			faker = new FakerWrapper(fakerString);
		} catch(IllegalArgumentException e) {
			throw CommandAPI.fail(msg.main("error.illegal_faker"));
		}

		Bond[] bonds = IntStream.range(1, bondQty + 1)
				.mapToObj(i -> hub.createBond(faker.getString())) //"Team" + i
				.peek(bond -> hub.setBondColor(bond, iterColors.next()))
				.toArray(Bond[]::new);
		AtomicInteger iBonds = new AtomicInteger(0);

		gamers.forEach(gamer -> hub.boundGamer(bonds[iBonds.getAndIncrement() % bondQty], gamer));

		MessageFormatter formatter = new MessageFormatter()
				.addColors();

		sender.sendMessage(formatter.custom("nBonds", bonds.length)
				.custom("nGamers", gamers.size())
				.formatFine(msg.game("bond.created_n")));

	}

}
