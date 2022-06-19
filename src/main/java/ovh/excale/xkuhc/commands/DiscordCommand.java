package ovh.excale.xkuhc.commands;

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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import ovh.excale.xkuhc.comms.MessageBundles;
import ovh.excale.xkuhc.comms.MessageFormatter;
import ovh.excale.xkuhc.configuration.ConfigKeys;
import ovh.excale.xkuhc.core.Game;
import ovh.excale.xkuhc.discord.DiscordEndpoint;
import ovh.excale.xkuhc.xkUHC;

@Alias("ds")
@Command("discord")
public class DiscordCommand {

	@Default
	public static void toggleEnable(CommandSender sender, @AMultiLiteralArgument({ "enable", "disable" }) String action) throws WrapperCommandSyntaxException {

		MessageBundles msg = xkUHC.instance()
				.getMessages();

		// TODO: GAME STATUS CHECK
		Game game = xkUHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.mainRaw("game.no_game"));

		switch(action) {

			case "enable" -> {

				if(DiscordEndpoint.getInstance() != null)
					throw CommandAPI.fail(msg.discord("integration.is_enabled")
							.format());

				// Get token and guildId from config file
				ConfigurationSection config = xkUHC.instance()
						.getConfig();

				String token = config.getString(ConfigKeys.DISCORD_TOKEN);
				long guildId = config.getLong(ConfigKeys.DISCORD_GUILD_ID);

				Bukkit.getScheduler()
						.runTaskAsynchronously(xkUHC.instance(), () -> {

							sender.sendMessage(msg.discord("integration.loading")
									.formatFine());

							try {

								DiscordEndpoint.open(token, guildId);
								sender.sendMessage(msg.discord("integration.enabled")
										.formatFine());

							} catch(Exception e) {

								sender.sendMessage(new MessageFormatter(e.getMessage()).addColors()
										.formatFail());
								DiscordEndpoint.close();

							}

						});

			}

			case "disable" -> {

				DiscordEndpoint endpoint = DiscordEndpoint.getInstance();

				if(endpoint == null)
					throw CommandAPI.fail(msg.discordRaw("integration.is_disabled"));

				Bukkit.getScheduler()
						.runTaskAsynchronously(xkUHC.instance(), () -> {

							try {

								DiscordEndpoint.close();
								sender.sendMessage(msg.discord("integration.disabled")
										.formatFail());

							} catch(Exception e) {

								sender.sendMessage(new MessageFormatter(e.getMessage()).addColors()
										.formatFail());
								DiscordEndpoint.close();

							}

						});

			}

			default -> throw CommandAPI.fail(msg.mainRaw("error.unknown_option"));

		}

	}

	@Subcommand("user")
	public static void linkUser(CommandSender sender, @APlayerArgument Player player, @ALongArgument long userId) throws WrapperCommandSyntaxException {

		MessageBundles msg = xkUHC.instance()
				.getMessages();

		DiscordEndpoint endpoint = DiscordEndpoint.getInstance();

		if(endpoint == null)
			throw CommandAPI.fail(msg.discordRaw("integration.not_enabled"));

		try {

			Member member = endpoint.linkPlayer(player, userId);
			sender.sendMessage(msg.discord("player.linked")
					.custom("gamer", player.getDisplayName())
					.custom("user", member.getDisplayName())
					.formatFine());

		} catch(IllegalArgumentException e) {
			throw CommandAPI.fail(e.getMessage());
		}

	}

	@Subcommand("mainchannel")
	public static void mainChannel(CommandSender sender, @ALongArgument long channelId) throws WrapperCommandSyntaxException {

		MessageBundles msg = xkUHC.instance()
				.getMessages();

		DiscordEndpoint endpoint = DiscordEndpoint.getInstance();

		if(endpoint == null)
			throw CommandAPI.fail(msg.discordRaw("integration.not_enabled"));

		try {

			VoiceChannel channel = endpoint.setMainChannel(channelId);
			sender.sendMessage(msg.discord("channel.set_main")
					.custom("channelName", channel.getName())
					.formatFine());

		} catch(IllegalArgumentException e) {
			throw CommandAPI.fail(e.getMessage());
		}

	}

}
