package ovh.excale.mc.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AChatColorArgument;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import dev.jorel.commandapi.arguments.ChatColorArgument;
import dev.jorel.commandapi.arguments.ListArgumentBuilder;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import ovh.excale.mc.UHC;
import ovh.excale.mc.uhc.Game;
import ovh.excale.mc.uhc.core.Bond;
import ovh.excale.mc.uhc.core.Gamer;
import ovh.excale.mc.uhc.core.GamerHub;
import ovh.excale.mc.utils.MessageBundles;
import ovh.excale.mc.utils.MessageFormatter;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Alias("bond")
@Command("bonds")
public class BondCommand {

	@Default
	public static void listBondsMain(CommandSender sender) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		Game game = UHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		GamerHub hub = game.getHub();
		StringBuilder list = new StringBuilder("\n~~~ BONDS ~~~");
		for(Bond bond : hub.getBonds()) {

			list.append("\n [")
					.append(bond.getColor())
					.append(bond.getName())
					.append(ChatColor.RESET)
					.append(']');

			for(Gamer gamer : bond.getGamers()) {

				list.append("\n - ")
						.append(gamer.getPlayer()
								.getDisplayName());

				if(!gamer.isOnline())
					list.append(" (OFFLINE)");

			}
		}

		list.append("\n\n");
		sender.sendMessage(list.toString());

	}

	@Subcommand("list")
	public static void listBonds(CommandSender sender) throws WrapperCommandSyntaxException {
		listBondsMain(sender);
	}

	@Subcommand("list")
	public static void listBondMembers(CommandSender sender,
			@AStringArgument String bondName) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		Game game = UHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		GamerHub hub = game.getHub();
		Bond bond = hub.getBond(bondName);

		if(bond == null)
			throw CommandAPI.fail(msg.main("bond.no_such"));

		StringBuilder list = new StringBuilder("\n-- " + bond.getColor() + bondName + ChatColor.RESET + " --");
		for(Gamer gamer : bond.getGamers()) {

			list.append("\n - ")
					.append(gamer.getPlayer()
							.getDisplayName());

			if(!gamer.isOnline())
				list.append(" (OFFLINE)");

		}

		list.append("\n\n");
		sender.sendMessage(list.toString());

	}

	@Subcommand("create")
	public static void createBond(CommandSender sender,
			@AStringArgument String bondName) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		Game game = UHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		Bond bond;

		try {

			bond = game.getHub()
					.createBond(bondName);

		} catch(IllegalStateException | IllegalArgumentException e) {
			throw CommandAPI.fail(e.getMessage());
		}

		sender.sendMessage(MessageFormatter.with(bond)
				.formatFine(msg.main("bond.created")));

	}

	@Subcommand("break")
	public static void breakBond(CommandSender sender,
			@AStringArgument String bondName) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		Game game = UHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		try {

			game.getHub()
					.removeBond(bondName);

		} catch(IllegalStateException | IllegalArgumentException e) {
			throw CommandAPI.fail(e.getMessage());
		}

		sender.sendMessage(new MessageFormatter().addColors()
				.bond(bondName)
				.formatFine(msg.main("bond.deleted")));

	}

	@Subcommand("bound")
	public static void boundGamer(CommandSender sender, @AStringArgument String bondName,
			@APlayerArgument Player target) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		Game game = UHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		Bond bond = game.getHub()
				.getBond(bondName);

		if(bond == null)
			throw CommandAPI.fail(msg.main("bond.no_such"));

		Gamer gamer;

		try {

			GamerHub hub = game.getHub();
			gamer = hub.getGamer(target.getUniqueId());

			if(gamer == null)
				gamer = hub.register(target);

			hub.boundGamer(bond, gamer);

		} catch(IllegalStateException | IllegalArgumentException e) {
			throw CommandAPI.fail(e.getMessage());
		}

		sender.sendMessage(MessageFormatter.with(gamer, bond)
				.formatFine(msg.main("bond.gamer_bound")));

	}

	@Subcommand("unbound")
	public static void unboundGamer(CommandSender sender,
			@APlayerArgument Player target) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		Game game = UHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		Gamer gamer;
		Bond bond;

		try {

			GamerHub hub = game.getHub();
			gamer = hub.getGamer(target.getUniqueId());

			if(gamer == null)
				throw CommandAPI.fail(new MessageFormatter().addColors()
						.gamer(target.getName())
						.format(msg.main("bond.gamer_no_bond")));

			bond = gamer.getBond();

			if(!gamer.hasBond())
				throw CommandAPI.fail(MessageFormatter.with(gamer)
						.format(msg.main("bond.gamer_no_bond")));

			hub.unboundGamer(gamer);

		} catch(IllegalStateException | IllegalArgumentException e) {
			throw CommandAPI.fail(e.getMessage());
		}

		sender.sendMessage(MessageFormatter.with(gamer, bond)
				.formatFine(msg.main("bond.gamer_unbound")));

	}

	@Subcommand("color")
	public static void getColor(CommandSender sender,
			@AStringArgument String bondName) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		Game game = UHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		Bond bond = game.getHub()
				.getBond(bondName);

		if(bond == null)
			throw CommandAPI.fail(msg.main("bond.no_such"));

		ChatColor color = bond.getColor();
		sender.sendMessage(bondName + " ~ " + color + color.name());

	}

	@Subcommand("color")
	public static void setColor(CommandSender sender, @AStringArgument String bondName,
			@AChatColorArgument ChatColor color) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		Game game = UHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		GamerHub hub = game.getHub();
		Bond bond = hub.getBond(bondName);

		if(bond == null)
			throw CommandAPI.fail(msg.main("bond.no_such"));

		if(!color.isColor())
			throw CommandAPI.fail(msg.main("error.illegal_color"));

		try {

			hub.setBondColor(bond, color);

		} catch(IllegalStateException e) {
			throw CommandAPI.fail(e.getMessage());
		}

		sender.sendMessage(MessageFormatter.with(bond)
				.formatFine(msg.main("bond.color_set")));

	}

	// bond create (name) (color) [players...]
	public static CommandAPICommand createBondFullArgs() {

		return new CommandAPICommand("bonds").withAliases("bond")
				.withArguments(new MultiLiteralArgument("create").setListed(false))
				.withArguments(new StringArgument("bondName"))
				.withArguments(new ChatColorArgument("bondColor"))
				.withArguments(new ListArgumentBuilder<Player>("playerNames").withList(() -> {

							final Set<UUID> boundPlayers = Optional.ofNullable(UHC.getGame())
									.map(Game::getHub)
									.stream()
									.flatMap(hub -> hub.getGamers()
											.stream())
									.filter(Gamer::hasBond)
									.map(Gamer::getPlayer)
									.map(Entity::getUniqueId)
									.collect(Collectors.toSet());

							return Bukkit.getOnlinePlayers()
									.stream()
									.filter(player -> !boundPlayers.contains(player.getUniqueId()))
									.map(OfflinePlayer::getPlayer)
									.toList();

						})
						.withMapper(HumanEntity::getName)
						.build())
				.executes((sender, args) -> {
					//noinspection unchecked
					createBondFullArgsImpl(sender, (String) args[0], (ChatColor) args[1], (List<Player>) args[2]);
				});

	}

	private static void createBondFullArgsImpl(CommandSender sender, String bondName, ChatColor bondColor,
			List<Player> players) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		Game game = UHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		GamerHub hub = game.getHub();

		try {

			Bond bond = hub.createBond(bondName);

			MessageFormatter formatter = MessageFormatter.with(bond);
			sender.sendMessage(formatter.formatFine(msg.main("bond.created")));

			hub.setBondColor(bond, bondColor);

			formatter.bond(bond);
			sender.sendMessage(formatter.formatFine(msg.main("bond.color_set")));

			for(Player player : players) {

				Gamer gamer = hub.getGamer(player.getUniqueId());
				if(gamer == null)
					gamer = hub.register(player);

				hub.boundGamer(bond, gamer);

			}

			sender.sendMessage(formatter.formatFine(msg.main("bond.gamers_bound")));

		} catch(IllegalStateException | IllegalArgumentException e) {
			throw CommandAPI.fail(e.getMessage());
		}

	}

}



















