package ovh.excale.mc.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import ovh.excale.mc.UHC;
import ovh.excale.mc.uhc.Game;
import ovh.excale.mc.uhc.misc.UhcWorldUtil;

import java.io.File;
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

	@Subcommand("discord")
	public static void discord(CommandSender sender, @AStringArgument String action) throws WrapperCommandSyntaxException {
		Game game = UHC.getGame();

		if(game == null)
			CommandAPI.fail("No game found");

		//TODO: Istance of discord client

		try {
			if(action.equalsIgnoreCase("enable")) {
				//TODO: connect discord bot

				sender.sendMessage("Discord enabled correctly.");
			} else if(action.equalsIgnoreCase("disable")) {
				/* TODO: disconnect discord bot
				if( == null)
					CommandAPI.fail("Cannot disable discord if it isn't enabled first!");

				 */
				sender.sendMessage("Discord disabled correctly.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			CommandAPI.fail("Error");
		}

	}

}
