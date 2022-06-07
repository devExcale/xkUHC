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
import ovh.excale.mc.utils.MessageBundles;
import ovh.excale.mc.utils.MessageFormatter;

@Alias("ds")
@Command("discord")
public class DiscordCommand {

	@Default
	public static void toggleEnable(CommandSender sender, @AMultiLiteralArgument({ "enable", "disable" }) String action) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		// TODO: GAME STATUS CHECK
		Game game = UHC.getGame();
		if(game == null)
			throw CommandAPI.fail(msg.main("game.no_game"));

		MessageFormatter formatter = new MessageFormatter();

		try {
			switch(action) {

				case "enable" -> {

					// Get token and guildId from config file
					ConfigurationSection config = UHC.instance()
							.getConfig();

					String token = config.getString("discord.token");
					long guildId = config.getLong("discord.guildId");

					try {

						DiscordEndpoint.open(token, guildId);

					} catch(IllegalStateException e) {
						throw CommandAPI.fail(msg.discord("integration.is_enabled"));
					}

					sender.sendMessage(formatter.formatFine(msg.discord("integration.enabled")));

				}

				case "disable" -> {

					DiscordEndpoint endpoint = DiscordEndpoint.getInstance();

					if(endpoint == null)
						throw CommandAPI.fail(msg.discord("integration.is_disabled"));

					DiscordEndpoint.close();

					sender.sendMessage(formatter.formatFine(msg.discord("integration.disabled")));

				}

				default -> throw CommandAPI.fail(msg.main("error.unknown_option"));

			}

		} catch(RuntimeException e) {

			DiscordEndpoint.close();
			throw CommandAPI.fail(e.getMessage());

		}

	}

	@Subcommand("user")
	public static void bindUser(CommandSender sender, @APlayerArgument Player player, @ALongArgument long userId) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		DiscordEndpoint endpoint = DiscordEndpoint.getInstance();

		if(endpoint == null)
			throw CommandAPI.fail(msg.discord("integration.not_enabled"));

		MessageFormatter formatter = new MessageFormatter();

		try {

			Member member = endpoint.bindPlayer(player, userId);
			sender.sendMessage(formatter.custom("gamer", player.getDisplayName())
					.custom("user", member.getDisplayName())
					.formatFine(msg.discord("player.linked")));

		} catch(IllegalArgumentException e) {
			throw CommandAPI.fail(e.getMessage());
		}

	}

	@Subcommand("mainchannel")
	public static void mainChannel(CommandSender sender, @ALongArgument long channelId) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		DiscordEndpoint endpoint = DiscordEndpoint.getInstance();

		if(endpoint == null)
			throw CommandAPI.fail(msg.discord("integration.not_enabled"));

		MessageFormatter formatter = new MessageFormatter();

		try {

			VoiceChannel channel = endpoint.setMainChannel(channelId);
			sender.sendMessage(formatter.custom("channelName", channel.getName())
					.formatFine(msg.discord("channel.set_main")));

		} catch(IllegalArgumentException e) {
			throw CommandAPI.fail(e.getMessage());
		}

	}

}
