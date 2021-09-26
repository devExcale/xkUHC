package ovh.excale.mc;

import dev.jorel.commandapi.CommandAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.excale.discord.DiscordEndpoint;
import ovh.excale.mc.commands.BondCommand;
import ovh.excale.mc.commands.ChatAllCommand;
import ovh.excale.mc.commands.DiscordCommand;
import ovh.excale.mc.commands.GameCommand;
import ovh.excale.mc.uhc.Game;
import ovh.excale.mc.uhc.Game.Status;
import ovh.excale.mc.uhc.misc.UhcWorldUtil;

import java.util.logging.Logger;

public class UHC extends JavaPlugin {

	public static boolean DEBUG;
	private static UHC instance;

	private Game coreGame;

	public static Plugin plugin() {
		return instance;
	}

	public static Logger logger() {
		return instance != null ? instance.getLogger() : Bukkit.getLogger();
	}

	public static Game getGame() {
		return instance.coreGame;
	}

	public static void setGame(Game game) {
		instance.coreGame = game;
	}

	@Override
	public void onLoad() {
		instance = this;

		saveDefaultConfig();
		saveResource("lang/game-messages.yml", false);

		//noinspection ConstantConditions
		DEBUG = Boolean.parseBoolean(getConfig().get("debug", false)
				.toString());

		CommandAPI.onLoad(DEBUG);
	}

	@Override
	public void onEnable() {

		CommandAPI.onEnable(this);

		if(Bukkit.getScoreboardManager() == null)
			throw new RuntimeException("Coudln't get ScoreboardManager. HINT: The plugin needs to load POST_WORLD.");

		UhcWorldUtil.purgeWorlds(worldCount -> logger().info("Removed " + worldCount + " world(s) from previous instances"));

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

}
