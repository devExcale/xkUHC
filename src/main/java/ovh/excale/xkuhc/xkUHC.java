package ovh.excale.xkuhc;

import dev.jorel.commandapi.CommandAPI;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.excale.xkuhc.commands.*;
import ovh.excale.xkuhc.comms.MessageBundles;
import ovh.excale.xkuhc.configuration.ConfigKeys;
import ovh.excale.xkuhc.core.Game;
import ovh.excale.xkuhc.discord.DiscordEndpoint;
import ovh.excale.xkuhc.world.WorldUtils;

import java.util.Optional;

public class xkUHC extends JavaPlugin {

	private static xkUHC instance;

	public static boolean DEBUG;

	public static xkUHC instance() {
		return instance;
	}

	public static Game getGame() {
		return instance.coreGame;
	}

	public static void setGame(Game game) {
		instance.coreGame = game;
	}

	public static void callAsync(Event event) {

		Bukkit.getScheduler()
				.runTaskAsynchronously(instance(), () -> Bukkit.getPluginManager()
						.callEvent(event));

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
					.map(count -> msg.mainRaw("misc.removed_worlds", count))
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

				if(coreGame.getPhase() == Game.Phase.RUNNING)
					coreGame.stop();
				coreGame.unset();

			} catch(IllegalStateException ignored) {
			}

	}

	public MessageBundles getMessages() {
		return msg;
	}

}
