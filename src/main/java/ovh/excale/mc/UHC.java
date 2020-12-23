package ovh.excale.mc;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.excale.mc.api.TeamedGame;
import ovh.excale.mc.utils.PlayerResponseListener;

import java.util.logging.Logger;

// TODO: CHECK PlayerDeathEvent LISTENER IN SESSION RUN
public class UHC extends JavaPlugin {

	public static boolean DEBUG_MODE = false;
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
	public void onEnable() {
		super.onEnable();
		instance = this;

		if(Bukkit.getScoreboardManager() == null)
			throw new RuntimeException("Coudln't get ScoreboardManager. HINT: The plugin needs to load POST_WORLD.");

		RandomUhcWorldGenerator.purgeWorlds(null);

		PlayerResponseListener playerResponseListener = new PlayerResponseListener(this, 10);
		Bukkit.getPluginManager()
				.registerEvents(playerResponseListener, this);

		// REGISTER COMMANDS
		Commands.TEAMS.register();
		Commands.XKUHC.register();

	}

}
