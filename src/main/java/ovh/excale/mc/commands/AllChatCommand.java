package ovh.excale.mc.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.arguments.AGreedyStringArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ovh.excale.mc.UHC;
import ovh.excale.mc.uhc.Game;
import ovh.excale.mc.uhc.core.Bond;
import ovh.excale.mc.uhc.core.Gamer;

@Alias("all")
@Command("allchat")
public class AllChatCommand {

	@Default
	public static void allChat(CommandSender sender,
			@AGreedyStringArgument GreedyStringArgument message) throws WrapperCommandSyntaxException {

		Game game = UHC.getGame();
		String finalMessage;

		if(sender instanceof Player) {

			Player player = (Player) sender;

			Gamer gamer = game.getHub()
					.getGamer(player.getUniqueId());

			if(!gamer.isAlive())
				CommandAPI.fail(
						"You can't use all chat if you are dead, your chat is arleady all with others dead players");

			Bond bond = gamer.getBond();

			finalMessage = bond.getColor() + "[All][" + bond.getName() + "]" + ChatColor.BOLD + sender.getName() +
					ChatColor.RESET + bond.getColor() + ": " + " " + message.toString();

		} else
			finalMessage = ChatColor.RED + "[CONSOLE] Admin: " + message.toString();

		game.getHub()
				.broadcast(finalMessage);

	}

}