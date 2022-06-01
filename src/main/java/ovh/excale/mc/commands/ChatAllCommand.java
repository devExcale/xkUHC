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

	public ChatAllCommand() {
	}

	@Default
	public static void chatAll(Player player,
			@AGreedyStringArgument String message) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		// TODO: GAME CHECK
		Game game = UHC.getGame();
		GamerHub hub = game.getHub();
		Gamer gamer = hub.getGamer(player.getUniqueId());

		if(!gamer.isAlive())
			throw CommandAPI.fail(msg.game("chat.dead_all_err"));

		hub.broadcast(MessageFormatter.with(gamer, gamer.getBond())
				.custom("message", message)
				.format(msg.game("chat.all")));

	}

	// TODO: think about if we need to create boolean allChatMuted and isAllChat(), muteAllChat() and unmuteAllChat() methods on Gamer and use these commands:
	public static void muteAllChat(Player player) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		throw CommandAPI.fail(msg.main("error.undeveloped"));

	}

	public static void unmuteAllChat(Player player) throws WrapperCommandSyntaxException {

		MessageBundles msg = UHC.instance()
				.getMessages();

		throw CommandAPI.fail(msg.main("error.undeveloped"));

	}

}