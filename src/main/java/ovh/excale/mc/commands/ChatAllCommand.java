package ovh.excale.mc.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.arguments.AGreedyStringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.entity.Player;
import ovh.excale.mc.UHC;
import ovh.excale.mc.uhc.Game;
import ovh.excale.mc.uhc.core.Gamer;
import ovh.excale.mc.uhc.core.GamerHub;
import ovh.excale.mc.utils.MessageBundles;
import ovh.excale.mc.utils.MessageFormatter;

@Alias("shout")
@Command("all")
public class ChatAllCommand {

	private final MessageBundles msg;

	public ChatAllCommand() {
		msg = UHC.instance()
				.getMessages();
	}

	@Default
	public static void chatAll(Player player,
			@AGreedyStringArgument String message) throws WrapperCommandSyntaxException {

		// TODO: GAME CHECK
		Game game = UHC.getGame();
		GamerHub hub = game.getHub();
		Gamer gamer = hub.getGamer(player.getUniqueId());

		if(!gamer.isAlive())
			throw CommandAPI.fail("You can only chat by default with dead players");

		MessageFormatter formatter = MessageFormatter.create()
				.with(gamer)
				.with(gamer.getBond());

		// ex: [All][BondName] Cr4zy5ky_U: this is a message
		hub.broadcast(formatter.format("[All]{bondColor}[{bond}] {BOLD}{gamer}{RESET}{bondColor}: {RESET}" + message));

	}

	// TODO: think about if we need to create boolean allChatMuted and isAllChat(), muteAllChat() and unmuteAllChat() methods on Gamer and use these commands:
	public static void muteAllChat(Player player) throws WrapperCommandSyntaxException {

		throw CommandAPI.fail("Command not developed yet!");

	}

	public static void unmuteAllChat(Player player) throws WrapperCommandSyntaxException {

		throw CommandAPI.fail("Command not developed yet!");

	}

}