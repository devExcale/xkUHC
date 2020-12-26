package ovh.excale.mc;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.excale.mc.api.TeamedGame;
import ovh.excale.mc.utils.Commands;
import ovh.excale.mc.utils.PlayerResponseListener;
import ovh.excale.mc.utils.RandomUhcWorldGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
		Commands.TEAMS.register();
		Commands.XKUHC.register();

		try(PrintWriter writer = new PrintWriter(new File(Bukkit.getUpdateFolderFile(), "Advancements.txt"))) {
			Bukkit.advancementIterator()
					.forEachRemaining(advancement -> {
						writer.println("\n[" + advancement.getKey() + "]");
						advancement.getCriteria()
								.forEach(writer::println);
					});
		} catch(FileNotFoundException e) {
			logger().log(Level.WARNING, e.getMessage(), e);
		}

		new CommandAPICommand("mad").withAliases("myAdvancements")
				.executesPlayer((player, objects) -> {

					try(PrintWriter writer = new PrintWriter(new File(Bukkit.getUpdateFolderFile(), "Advancements.txt"))) {
						Bukkit.advancementIterator()
								.forEachRemaining(advancement -> {

									writer.println("\n[" + advancement.getKey() + "]");
									AdvancementProgress progress = player.getAdvancementProgress(advancement);
									progress.getAwardedCriteria()
											.forEach(s -> writer.println(" + " + s));
									progress.getRemainingCriteria()
											.forEach(s -> writer.println(" - " + s));

								});
					} catch(FileNotFoundException e) {
						logger().log(Level.WARNING, e.getMessage(), e);
						player.sendMessage(e.getMessage());
					}

				})
				.register();

		new CommandAPICommand("removeStoryAdv").withAliases("rsa")
				.executesPlayer((player, objects) -> {

					Bukkit.advancementIterator()
							.forEachRemaining(advancement -> {
								if(advancement.getKey()
										.getKey()
										.contains("story")) {
									AdvancementProgress progress = player.getAdvancementProgress(advancement);
									for(String awardedCriteria : progress.getAwardedCriteria())
										progress.revokeCriteria(awardedCriteria);
								}
							});

				})
				.register();

	}

}
