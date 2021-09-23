package ovh.excale.mc.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import discord4j.core.GatewayDiscordClient;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import ovh.excale.discord.DiscordEndpoint;
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

		try {
			switch(action.toLowerCase()) {

				case "enable":

					// get token and guild
					ConfigurationSection config = UHC.plugin()
							.getConfig();
					String token = config.getString("discord.token");
					long guildId = config.getInt("discord.guildId");

					try {
						DiscordEndpoint.open(token, guildId);
					} catch(IllegalStateException e) {
						CommandAPI.fail("Discord integration already enabled");
					}

					sender.sendMessage("Discord integration successfully enabled");

					break;

				case "disable":

					DiscordEndpoint endpoint = DiscordEndpoint.getInstance();
					if(endpoint == null)
						CommandAPI.fail("Discord integration is already disabled");

					DiscordEndpoint.close();
					sender.sendMessage("Discord integration disabled");

					break;

				default:
					CommandAPI.fail("Unknown option");

			}
		} catch(RuntimeException e) {
			CommandAPI.fail(e.getMessage());
		}

	}

}
