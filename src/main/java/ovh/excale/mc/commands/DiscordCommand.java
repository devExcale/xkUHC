package ovh.excale.mc.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.ALongArgument;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ovh.excale.discord.DiscordEndpoint;

@Alias("ds")
@Command("discord")
public class DiscordCommand {

	@Subcommand("user")
	public static void discord(CommandSender sender, @APlayerArgument Player player, @ALongArgument long userId) throws WrapperCommandSyntaxException {

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
