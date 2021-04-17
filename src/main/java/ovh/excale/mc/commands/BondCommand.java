package ovh.excale.mc.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AChatColorArgument;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ovh.excale.mc.UHC;
import ovh.excale.mc.uhc.Game;
import ovh.excale.mc.uhc.core.Bond;
import ovh.excale.mc.uhc.core.Gamer;
import ovh.excale.mc.uhc.core.GamerHub;

@Alias("bond")
@Command("bonds")
public class BondCommand {

	@Default
	public static void listBondsMain(CommandSender sender) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game == null)
			CommandAPI.fail("No game found");

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
	public static void listBondMembers(CommandSender sender, @AStringArgument String bondName) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game == null)
			CommandAPI.fail("No game found");

		GamerHub hub = game.getHub();
		Bond bond = hub.getBond(bondName);

		if(bond == null)
			CommandAPI.fail("No such bond");

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
	public static void createBond(CommandSender sender, @AStringArgument String bondName) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game == null)
			CommandAPI.fail("No game found");

		try {

			game.getHub()
					.createBond(bondName);

		} catch(IllegalStateException | IllegalArgumentException e) {
			CommandAPI.fail(e.getMessage());
		}

		sender.sendMessage("Bond created successfully");

	}

//	@Subcommand("create")
//	public static void createBondColor(CommandSender sender, @AStringArgument String bondName, @AChatColorArgument ChatColor color) throws WrapperCommandSyntaxException {
//
//		Game game = UHC.getGame();
//
//		if(game == null)
//			CommandAPI.fail("No game found");
//
//		BondManager bondManager = game.getBondManager();
//
//		try {
//
//			game.getHub()
//					.createBond(bondName)
//					.setColor(color);
//
//		} catch(IllegalStateException | IllegalArgumentException e) {
//			CommandAPI.fail(e.getMessage());
//		}
//
//		sender.sendMessage("Bond created successfully");
//
//	}

	@Subcommand("break")
	public static void breakBond(CommandSender sender, @AStringArgument String bondName) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game == null)
			CommandAPI.fail("No game found");

		try {

			game.getHub()
					.removeBond(bondName);

		} catch(IllegalStateException | IllegalArgumentException e) {
			CommandAPI.fail(e.getMessage());
		}

		sender.sendMessage("Bond broken successfully");

	}

	@Subcommand("bound")
	public static void boundGamer(CommandSender sender, @AStringArgument String bondName, @APlayerArgument Player target) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game == null)
			CommandAPI.fail("No game found");

		Bond bond = game.getHub()
				.getBond(bondName);

		if(bond == null)
			CommandAPI.fail("No such bond");

		try {

			GamerHub hub = game.getHub();

			Gamer gamer = hub.getGamer(target.getUniqueId());
			if(gamer == null)
				gamer = hub.register(target);

			hub.boundGamer(bond, gamer);

		} catch(IllegalStateException | IllegalArgumentException e) {
			CommandAPI.fail(e.getMessage());
		}

		sender.sendMessage("Gamer bound successfully");

	}

	@Subcommand("unbound")
	public static void unboundGamer(CommandSender sender, @APlayerArgument Player target) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game == null)
			CommandAPI.fail("No game found");

		try {

			GamerHub hub = game.getHub();

			Gamer gamer = hub.getGamer(target.getUniqueId());
			if(gamer == null || !gamer.hasBond())
				throw new IllegalArgumentException("Gamer doesn't have a bond");

			hub.unboundGamer(gamer);

		} catch(IllegalStateException | IllegalArgumentException e) {
			CommandAPI.fail(e.getMessage());
		}

		sender.sendMessage("Gamer unbound successfully");

	}

	@Subcommand("color")
	public static void getColor(CommandSender sender, @AStringArgument String bondName) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game == null)
			CommandAPI.fail("No game found");

		Bond bond = game.getHub()
				.getBond(bondName);

		if(bond == null)
			CommandAPI.fail("No such bond");

		ChatColor color = bond.getColor();
		sender.sendMessage(bondName + " ~ " + color + color.name());

	}

	@Subcommand("color")
	public static void getColor(CommandSender sender, @AStringArgument String bondName, @AChatColorArgument ChatColor color) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game == null)
			CommandAPI.fail("No game found");

		GamerHub hub = game.getHub();
		Bond bond = hub.getBond(bondName);

		if(bond == null)
			CommandAPI.fail("No such bond");

		if(!color.isColor())
			CommandAPI.fail("Illegal color");

		try {

			hub.setBondColor(bond, color);

		} catch(IllegalStateException e) {
			CommandAPI.fail(e.getMessage());
		}

		sender.sendMessage("Successfully set bond color");

	}

}



















