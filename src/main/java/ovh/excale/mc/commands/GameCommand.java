package ovh.excale.mc.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AIntegerArgument;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import ovh.excale.mc.UHC;
import ovh.excale.mc.uhc.Game;
import ovh.excale.mc.uhc.core.Bond;
import ovh.excale.mc.uhc.core.Gamer;
import ovh.excale.mc.uhc.core.GamerHub;
import ovh.excale.mc.uhc.misc.UhcWorldUtil;

import java.io.File;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

@Alias("xkuhc")
@Command("uhc")
public class GameCommand {

	@Subcommand("create")
	public static void createGame(CommandSender sender) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game != null)
			CommandAPI.fail("There's already another session");

		try {

			File file = new File(UHC.plugin()
					.getDataFolder(), "lang/game-messages.yml");

			if(!file.canRead())
				CommandAPI.fail("Cannot get game messages bundle.");

			UHC.setGame(new Game(YamlConfiguration.loadConfiguration(file)));

		} catch(Exception e) {
			UHC.logger()
					.log(Level.SEVERE, e.getMessage(), e);
			CommandAPI.fail("There has been an internal error");
		}

		sender.sendMessage("Game created");

	}

	@Subcommand("start")
	public static void startGame(CommandSender sender) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game == null)
			CommandAPI.fail("No game found");

		try {

			game.tryStart();

		} catch(IllegalStateException e) {
			CommandAPI.fail(e.getMessage());
		} catch(Exception e) {
			UHC.logger()
					.log(Level.SEVERE, e.getMessage(), e);
			CommandAPI.fail("There has been an internal error");
		}

	}

	@Subcommand("stop")
	public static void stopGame(CommandSender sender) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game == null)
			CommandAPI.fail("No game found");

		try {

			game.stop();
			game.getHub()
					.broadcast("Game stopped forcefully.");
			UHC.setGame(null);

		} catch(IllegalStateException e) {
			CommandAPI.fail(e.getMessage());
		} catch(Exception e) {
			UHC.logger()
					.log(Level.SEVERE, e.getMessage(), e);
			CommandAPI.fail("There has been an internal error");
		}

	}

	@Subcommand("dump")
	public static void dump(CommandSender sender) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game == null)
			CommandAPI.fail("No game found");

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

		UhcWorldUtil.purgeWorlds(count -> sender.sendMessage("Removed " + count + " world(s) from previous instances"));

	}

	@Subcommand("reload")
	public static void reloadConfiguration(CommandSender sender) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();

		if(game != null && !game.getStatus()
				.isEditable()) {

			Plugin plugin = UHC.plugin();
			plugin.reloadConfig();
			UHC.DEBUG = plugin.getConfig()
					.getBoolean("debug", false);

		} else
			CommandAPI.fail("Game is running");

		sender.sendMessage("Configuration reloaded.");

	}

	@Subcommand("random")
	public static void listBondMembers(CommandSender sender, @AIntegerArgument Integer teamNumber) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();
		GamerHub hub = game.getHub();

		if(game == null)
			CommandAPI.fail("No game found");

		Bukkit.getServer().getOnlinePlayers().forEach(player -> game.getHub().register(player));

		Set<Gamer> gamers = hub.getGamers();

		int gamersNumber = gamers.size();

		if(gamersNumber < teamNumber)
			CommandAPI.fail("Can't create a number of teams greater than the gamers");
		else if(gamersNumber < 2)
			CommandAPI.fail("Can't create teams with only one gamer");
		else if(teamNumber < 2)
			CommandAPI.fail("Can't create less than two teams");
		else if(gamersNumber % teamNumber != 0)
			CommandAPI.fail("Can't create teams with the same players number");

		for(int i = 1; i <= teamNumber; i++)
			try {
				hub.createBond("Team" + i);
			} catch(IllegalStateException | IllegalArgumentException e) {
				CommandAPI.fail(e.getMessage());
			}

		Set<Bond> bonds = hub.getBonds();

		Random random = new Random();

		for(Bond bond : bonds) {
			int index = random.nextInt(gamersNumber);
			for(int i = 1; i <= gamersNumber / teamNumber; i++) {
				if(bonds.stream()
						.noneMatch(bond1 -> bond1.getGamers()
								.equals(gamers.toArray()[index + 1])))
					try {

						hub.boundGamer(bond, hub.getGamer(((Gamer) gamers.toArray()[index + 1]).getUniqueId()));

					} catch(IllegalStateException | IllegalArgumentException e) {
						CommandAPI.fail(e.getMessage());
					}
				else
					i--;
			}

			Set<ChatColor> usedColors = hub.getBondColors();
			ChatColor[] allColors = ChatColor.values();
			int randColor;
			ChatColor bondColor;

			do {
				randColor = random.nextInt(22);
				bondColor = allColors[randColor];
				for(ChatColor usedColor : usedColors) {
					if(bondColor.equals(usedColor)) {
						bondColor = ChatColor.WHITE;
						break;
					}
				}
			} while (!bondColor.isColor() || bondColor.equals(ChatColor.WHITE));

			try {

				hub.setBondColor(bond, bondColor);

			} catch(IllegalStateException e) {
				CommandAPI.fail(e.getMessage());
			}
		}
	}

}
