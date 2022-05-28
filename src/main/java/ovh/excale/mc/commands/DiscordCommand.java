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
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
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
					ConfigurationSection config = UHC.instance()
							.getConfig();
					String token = config.getString("discord.token");
					long guildId = config.getLong("discord.guildId");

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

			Member member = endpoint.bindPlayer(player, userId);
			sender.sendMessage("Player " + player.getDisplayName() + " bound correctly to " + member.getDisplayName());

		} catch(IllegalArgumentException e) {
			CommandAPI.fail(e.getMessage());
		}

	}

	@Subcommand("mainchannel")
	public static void mainChannel(CommandSender sender, @ALongArgument long channelId) throws WrapperCommandSyntaxException {

		DiscordEndpoint endpoint = DiscordEndpoint.getInstance();

		if(endpoint == null)
			CommandAPI.fail("Discord integration is not enabled");

		try {

			VoiceChannel channel = endpoint.setMainChannel(channelId);
			sender.sendMessage("Set <" + channel.getName() + "> as main channel");

		} catch(IllegalArgumentException e) {
			CommandAPI.fail(e.getMessage());
		}

	}

}
