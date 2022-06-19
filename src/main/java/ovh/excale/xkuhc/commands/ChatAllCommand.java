package ovh.excale.xkuhc.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.arguments.AGreedyStringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.entity.Player;
import ovh.excale.xkuhc.comms.MessageBundles;
import ovh.excale.xkuhc.core.Game;
import ovh.excale.xkuhc.core.Gamer;
import ovh.excale.xkuhc.core.GamerHub;
import ovh.excale.xkuhc.xkUHC;

@Alias("shout")
@Command("all")
public class ChatAllCommand {

	public ChatAllCommand() {
	}

	@Default
	public static void chatAll(Player player, @AGreedyStringArgument String message) throws WrapperCommandSyntaxException {

		MessageBundles msg = xkUHC.instance()
				.getMessages();

		// TODO: GAME CHECK
		Game game = xkUHC.getGame();
		GamerHub hub = game.getHub();
		Gamer gamer = hub.getGamer(player.getUniqueId());

		if(!gamer.isAlive())
			throw CommandAPI.fail(msg.gameRaw("chat.dead_all_err"));

		hub.broadcast(msg.game("chat.all")
				.gamer(gamer)
				.bond(gamer.getBond())
				.custom("message", message)
				.format());

	}

	// TODO: think about if we need to create boolean allChatMuted and isAllChat(), muteAllChat() and unmuteAllChat() methods on Gamer and use these commands:
	public static void muteAllChat(Player player) throws WrapperCommandSyntaxException {

		MessageBundles msg = xkUHC.instance()
				.getMessages();

		throw CommandAPI.fail(msg.mainRaw("error.undeveloped"));

	}

	public static void unmuteAllChat(Player player) throws WrapperCommandSyntaxException {

		MessageBundles msg = xkUHC.instance()
				.getMessages();

		throw CommandAPI.fail(msg.mainRaw("error.undeveloped"));

	}

}