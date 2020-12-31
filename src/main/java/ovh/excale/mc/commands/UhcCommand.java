package ovh.excale.mc.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ovh.excale.mc.UHC;
import ovh.excale.mc.api.UhcGame;
import ovh.excale.mc.api.Challenger;
import ovh.excale.mc.api.Game;

import java.util.UUID;

@Alias("xkuhc")
@Command("uhc")
public class UhcCommand {

	protected static UhcGame getGame() {
		UHC plugin = (UHC) UHC.plugin();
		return (UhcGame) plugin.getGame();
	}

	protected static void validateAdmin(Game game, Player player) throws WrapperCommandSyntaxException {

		UUID playerId = player.getUniqueId();
		if(!playerId.equals(game.getAdminId()))
			CommandAPI.fail("You're not the admin of this game");

	}

	@Subcommand("create")
	public static void createGame(Player sender) throws WrapperCommandSyntaxException {

		UHC plugin = (UHC) UHC.plugin();
		Game game = plugin.getGame();

		if(game != null)
			CommandAPI.fail("There's already another session");

		plugin.setGame(new UhcGame(sender));
		sender.sendMessage("Game created!");

	}

	@Subcommand("start")
	public static void startGame(Player sender) throws WrapperCommandSyntaxException {

		Game game = getGame();

		if(game == null)
			CommandAPI.fail("No game found");

		validateAdmin(game, sender);

		try {
			game.start();
		} catch(IllegalStateException e) {
			CommandAPI.fail(e.getMessage());
		}

	}

	@Subcommand("stop")
	public static void stopGame(Player sender) throws WrapperCommandSyntaxException {

		Game game = getGame();

		if(game == null)
			CommandAPI.fail("No game found");

		validateAdmin(game, sender);

		try {
			game.stop();
		} catch(IllegalStateException e) {
			CommandAPI.fail(e.getMessage());
		}

	}

	@Subcommand("reset")
	public static void resetGame(Player sender) throws WrapperCommandSyntaxException {

		Game game = getGame();

		if(game == null)
			CommandAPI.fail("No game found");

		validateAdmin(game, sender);

		try {
			game.reset();
		} catch(IllegalStateException e) {
			CommandAPI.fail(e.getMessage());
		}

	}

	@Subcommand("status")
	public static void printStatus(CommandSender sender) throws WrapperCommandSyntaxException {

		UhcGame game = getGame();

		if(game == null)
			CommandAPI.fail("No game found");

		Challenger admin = game.getChallengerManager()
				.get(game.getAdminId());

		if(admin == null)
			CommandAPI.fail("Can't get game admin. You should forcefully create a new game.");

		Player playerAdmin = admin.vanilla();


		//noinspection StringBufferReplaceableByString
		StringBuilder sb = new StringBuilder("\n[xkUHC]\n - Admin: ");
		sb.append(playerAdmin.getDisplayName())
				.append(" (")
				.append(admin.isOnline() ? "ONLINE" : "OFFLINE")
				.append(")\n - Debug: ")
				.append(UHC.DEBUG ? "on" : "off")
				.append("\n - Status: ")
				.append(game.getStatus())
				.append('\n');

		sender.sendMessage(sb.toString());

	}

}
