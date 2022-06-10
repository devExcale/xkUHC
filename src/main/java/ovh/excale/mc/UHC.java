package ovh.excale.mc;

import dev.jorel.commandapi.CommandAPI;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.excale.discord.DiscordEndpoint;
import ovh.excale.mc.commands.*;
import ovh.excale.mc.uhc.Game;
import ovh.excale.mc.uhc.Game.Status;
import ovh.excale.mc.uhc.configuration.ConfigKeys;
import ovh.excale.mc.uhc.world.WorldUtils;
import ovh.excale.mc.utils.MessageBundles;

import java.util.Optional;
import java.util.logging.Logger;

public class UHC extends JavaPlugin {

	private static UHC instance;

	public static boolean DEBUG;

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

	private boolean delWorlds = true;

	@Override
	public void onLoad() {
		instance = this;

		saveDefaultConfig();
		saveResource("messages/game.yml", false);

		msg = new MessageBundles(this);

		FileConfiguration config = getConfig();

		DEBUG = config.getBoolean(ConfigKeys.DEBUG, false);
		delWorlds = config.getBoolean(ConfigKeys.DEL_WORLDS, true);

	}

	@Override
	public void onEnable() {

		PaperLib.suggestPaper(this);

		if(delWorlds)
			WorldUtils.purgeWorlds(worldCount -> Optional.of(worldCount)
					.filter(count -> count > 0)
					.map(count -> msg.main("misc.removed_worlds", count))
					.ifPresent(getLogger()::info));

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
