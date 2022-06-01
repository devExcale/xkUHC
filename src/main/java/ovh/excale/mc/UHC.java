package ovh.excale.mc;

import dev.jorel.commandapi.CommandAPI;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.excale.discord.DiscordEndpoint;
import ovh.excale.mc.commands.*;
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

		PaperLib.suggestPaper(this);

		// TODO: boolean option: if true delete worlds, otherwise don't
		WorldUtils.purgeWorlds(worldCount -> Optional.of(worldCount)
				.filter(count -> count > 0)
				.map(count -> msg.main("misc.removed_worlds", count))
				.ifPresent(log()::info));

		// REGISTER COMMANDS
		CommandAPI.registerCommand(GameCommand.class);
		CommandAPI.registerCommand(BondCommand.class);
		CommandAPI.registerCommand(DiscordCommand.class);
		CommandAPI.registerCommand(ChatAllCommand.class);

		CommandAPI.registerCommand(TestCommand.class);
		BondCommand.createBondFullArgs()
				.register();

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
