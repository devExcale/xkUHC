package ovh.excale.xkuhc.commands;

import com.github.javafaker.Faker;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AIntegerArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import ovh.excale.xkuhc.comms.MessageBundles;
import ovh.excale.xkuhc.comms.MessageFormatter;
import ovh.excale.xkuhc.configuration.ConfigKeys;
import ovh.excale.xkuhc.core.Bond;
import ovh.excale.xkuhc.core.Game;
import ovh.excale.xkuhc.core.Gamer;
import ovh.excale.xkuhc.core.GamerHub;
import ovh.excale.xkuhc.world.WorldUtils;
import ovh.excale.xkuhc.xkUHC;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.logging.Level.WARNING;

@Alias("xkuhc")
@Command("uhc")
public class GameCommand {

	private static Logger LOG_DO_NOT_REFERENCE = null;

	private static Logger log() {

		if(LOG_DO_NOT_REFERENCE == null)
			LOG_DO_NOT_REFERENCE = xkUHC.instance()
					.getLogger();

		return LOG_DO_NOT_REFERENCE;
	}

	@Subcommand("create")
	public static void createGame(CommandSender sender) throws WrapperCommandSyntaxException {

		MessageBundles msg = xkUHC.instance()
				.getMessages();

		Game game = xkUHC.getGame();
		if(game != null)
			throw CommandAPI.fail(msg.main("game.another_session"));

		try {

			xkUHC.setGame(new Game());

		} catch(Exception e) {

			log().log(Level.SEVERE, e.getMessage(), e);
			throw CommandAPI.fail(msg.main("error.internal"));

		}

		sender.sendMessage(new MessageFormatter().addColors()
				.formatFine(msg.main("game.created")));

	}

	@Subcommand("start")
	public static void startGame(CommandSender sender) throws WrapperCommandSyntaxException {

		MessageBundles msg = xkUHC.instance()
				.getMessages();

		Game game = xkUHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		try {

			game.tryStart();

		} catch(IllegalStateException e) {

			throw CommandAPI.fail(e.getMessage());

		} catch(Exception e) {

			log().log(Level.SEVERE, e.getMessage(), e);
			throw CommandAPI.fail(msg.main("error.internal"));

		}

	}

	@Subcommand("confirm")
	public static void confirmStart(CommandSender sender) throws WrapperCommandSyntaxException {

		MessageBundles msg = xkUHC.instance()
				.getMessages();

		Game game = xkUHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		game.setConfirmStart(true);

		sender.sendMessage(new MessageFormatter().formatFine(msg.main("game.confirmed")));

	}

	@Subcommand("stop")
	public static void stopGame(CommandSender sender) throws WrapperCommandSyntaxException {

		MessageBundles msg = xkUHC.instance()
				.getMessages();

		Game game = xkUHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		try {

			game.stop();
			game.getHub()
					.broadcast(msg.game("game.stop_force"));
			xkUHC.setGame(null);

		} catch(IllegalStateException e) {

			throw CommandAPI.fail(e.getMessage());

		} catch(Exception e) {

			log().log(Level.SEVERE, e.getMessage(), e);
			throw CommandAPI.fail(msg.main("error.internal"));

		}

	}

	@Subcommand("dump")
	public static void dump(CommandSender sender) throws WrapperCommandSyntaxException {

		MessageBundles msg = xkUHC.instance()
				.getMessages();

		Game game = xkUHC.getGame();
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

		MessageBundles msg = xkUHC.instance()
				.getMessages();

		WorldUtils.purgeWorlds(count -> sender.sendMessage(msg.main("misc.removed_worlds", count)));

	}

	@Subcommand("reload")
	public static void reloadConfiguration(CommandSender sender) throws WrapperCommandSyntaxException {

		xkUHC instance = xkUHC.instance();
		MessageBundles msg = instance.getMessages();

		Game game = xkUHC.getGame();

		if(game != null && !game.getStatus()
				.isEditable())
			throw CommandAPI.fail(msg.main("game.running"));

		instance.reloadConfig();
		xkUHC.DEBUG = instance.getConfig()
				.getBoolean(ConfigKeys.DEBUG, false);

		sender.sendMessage(new MessageFormatter().addColors()
				.formatFine(msg.main("config.reloaded")));

	}

	@Subcommand("random")
	public static void createRandomTeams(CommandSender sender, @AIntegerArgument Integer bondQty) throws WrapperCommandSyntaxException {

		MessageBundles msg = xkUHC.instance()
				.getMessages();

		if(bondQty < 2)
			throw CommandAPI.fail(msg.main("bond.less_two"));

		Game game = xkUHC.getGame();

		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		// TODO: check for other bonds / game status

		GamerHub hub = game.getHub();

		Set<UUID> bounded = hub.getGamers()
				.stream()
				.filter(Gamer::hasBond)
				.map(Gamer::getUniqueId)
				.collect(Collectors.toSet());

		List<Gamer> gamers = Bukkit.getServer()
				.getOnlinePlayers()
				.stream()
				.filter(player -> !bounded.contains(player.getUniqueId()))
				.map(player -> Optional.ofNullable(hub.getGamer(player.getUniqueId()))
						.orElseGet(() -> hub.register(player)))
				.collect(Collectors.toCollection(ArrayList::new));

		if(gamers.size() == 0)
			throw CommandAPI.fail(msg.main("bond.gamers_all_bound"));

		Collections.shuffle(gamers);

		if(gamers.size() < bondQty)
			throw CommandAPI.fail(msg.main("bond.too_many_bounds"));

		// Get colors
		Iterator<ChatColor> iColors = Arrays.stream(ChatColor.values())
				.filter(ChatColor::isColor)
				.collect(Collectors.collectingAndThen(Collectors.toList(), colors -> {

					Collections.shuffle(colors);
					return colors;

				}))
				.iterator();

		String fakerKey = xkUHC.instance()
				.getConfig()
				.getString(ConfigKeys.FAKER_KEY, "esport.teams");

		Faker faker = new Faker(new Locale(xkUHC.instance()
				.getConfig()
				.getString(ConfigKeys.FAKER_LOCALE, "EN")));

		try {

			faker.resolve(fakerKey);

		} catch(Exception e) {

			log().log(WARNING, "Key: " + fakerKey, e);
			throw CommandAPI.fail(msg.main("error.illegal_faker_key"));

		}

		Set<String> bondNames = Stream.generate(() -> faker.resolve(fakerKey))
				.distinct()
				.limit(bondQty)
				.filter(s -> s.length() <= 24)
				.map(s -> s.replaceAll(" ", "_"))
				.collect(Collectors.toSet());

		if(bondNames.size() != bondQty)
			throw CommandAPI.fail(msg.main("error.illegal_faker_value"));

		Iterator<String> iNames = bondNames.iterator();

		Bond[] bonds = Stream.generate(() -> hub.createBond(iNames.next()))
				.limit(bondQty)
				.peek(bond -> hub.setBondColor(bond, iColors.next()))
				.toArray(Bond[]::new);

		AtomicInteger iBonds = new AtomicInteger(0);

		gamers.forEach(gamer -> hub.boundGamer(bonds[iBonds.getAndIncrement() % bondQty], gamer));

		sender.sendMessage(new MessageFormatter().addColors()
				.custom("nBonds", bonds.length)
				.custom("nGamers", gamers.size())
				.formatFine(msg.main("bond.created_n")));

	}

}
