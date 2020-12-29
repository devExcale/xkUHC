package ovh.excale.mc;

import dev.jorel.commandapi.CommandAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.excale.mc.api.TeamedGame;
import ovh.excale.mc.commands.TeamsCommand;
import ovh.excale.mc.commands.UhcCommand;
import ovh.excale.mc.utils.PlayerResponseListener;
import ovh.excale.mc.utils.RandomUhcWorldGenerator;

import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: CHECK PlayerDeathEvent LISTENER IN SESSION RUN
public class UHC extends JavaPlugin {

	public static boolean DEBUG;
	private static UHC instance;

	public static Plugin plugin() {
		return instance;
	}

	public static Logger logger() {
		return instance != null ? instance.getLogger() : Bukkit.getLogger();
	}

	private TeamedGame game;

	public void setGame(TeamedGame game) {
		this.game = game;
	}

	public TeamedGame getGame() {
		return game;
	}

	@Override
	public void onLoad() {
		instance = this;

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

		RandomUhcWorldGenerator.purgeWorlds(worldCount -> logger().log(Level.INFO,
				"Removed " + worldCount + " world from previous instances!"));

		Bukkit.getPluginManager()
				.registerEvents(new PlayerResponseListener(this, 10), this);

		// REGISTER COMMANDS
		CommandAPI.registerCommand(UhcCommand.class);
		CommandAPI.registerCommand(TeamsCommand.class);

	}

	@Override
	public void onDisable() {

		if(game != null) {

			UhcGame game = (UhcGame) this.game;
			game.getTeamManager()
					.unregisterAll();
			game.getChallengerManager()
					.reset();

		}

	}

}
