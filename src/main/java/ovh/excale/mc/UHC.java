package ovh.excale.mc;

import dev.jorel.commandapi.CommandAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.excale.discord.DiscordEndpoint;
import ovh.excale.mc.commands.BondCommand;
import ovh.excale.mc.commands.ChatAllCommand;
import ovh.excale.mc.commands.DiscordCommand;
import ovh.excale.mc.commands.GameCommand;
import ovh.excale.mc.uhc.Game;
import ovh.excale.mc.uhc.Game.Status;
import ovh.excale.mc.uhc.world.WorldUtils;
import ovh.excale.mc.utils.MessageBundles;

import java.util.Optional;
import java.util.logging.Logger;

public class UHC extends JavaPlugin {

	public static boolean DEBUG;
	private static UHC instance;

	public static UHC instance() {
		return instance;
	}

	public static Logger log() {
		return instance != null ? instance.getLogger() : Bukkit.getLogger();
	}

	public static Game getGame() {
		return instance.coreGame;
	}

	public static void setGame(Game game) {
		instance.coreGame = game;
	}

	private Game coreGame;
	private MessageBundles msg;

	@Override
	public void onLoad() {
		instance = this;

		saveDefaultConfig();
		saveResource("messages/game.yml", false);

		msg = new MessageBundles(this);

		DEBUG = Boolean.parseBoolean(getConfig().get("debug", false)
				.toString());

	}

	@Override
	public void onEnable() {

		// TODO: boolean option: if true delete worlds, otherwise don't
		WorldUtils.purgeWorlds(worldCount -> Optional.of(worldCount)
				.filter(count -> count > 0)
				.map(count -> msg.main("removed_worlds", count))
				.ifPresent(log()::info));

		// REGISTER COMMANDS
		CommandAPI.registerCommand(GameCommand.class);
		CommandAPI.registerCommand(BondCommand.class);
		CommandAPI.registerCommand(DiscordCommand.class);
		CommandAPI.registerCommand(ChatAllCommand.class);

	}

	@Override
	public void onDisable() {

		DiscordEndpoint.close();

		if(coreGame != null)
			try {

				if(coreGame.getStatus() == Status.RUNNING)
					coreGame.stop();
				coreGame.dispose();

			} catch(IllegalStateException ignored) {
			}

	}

	public MessageBundles getMessages() {
		return msg;
	}

}
