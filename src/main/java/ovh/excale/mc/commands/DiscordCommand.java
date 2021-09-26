package ovh.excale.mc.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.ALongArgument;
import dev.jorel.commandapi.annotations.arguments.AMultiLiteralArgument;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import ovh.excale.discord.DiscordEndpoint;
import ovh.excale.mc.UHC;
import ovh.excale.mc.uhc.Game;

@Alias("ds")
@Command("discord")
public class DiscordCommand {

	@Default
	public static void toggleEnable(CommandSender sender, @AMultiLiteralArgument({ "enable", "disable" }) String action) throws WrapperCommandSyntaxException {

		// TODO: GAME STATUS CHECK
		Game game = UHC.getGame();

		if(game == null)
			CommandAPI.fail("No game found");

		try {
			switch(action) {
				case "enable":

					// Get token and guildId from config file
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
			DiscordEndpoint.close();
			CommandAPI.fail(e.getMessage());
		}

	}

	@Subcommand("user")
	public static void bindUser(CommandSender sender, @APlayerArgument Player player, @ALongArgument long userId) throws WrapperCommandSyntaxException {

		DiscordEndpoint endpoint = DiscordEndpoint.getInstance();

		if(endpoint == null)
			CommandAPI.fail("Discord integration is not enabled");

		try {

			endpoint.bindPlayer(player, userId);
			sender.sendMessage("Player bound correctly to the given Discord user id");

		} catch(IllegalArgumentException e) {
			CommandAPI.fail(e.getMessage());
		}

	}

}
