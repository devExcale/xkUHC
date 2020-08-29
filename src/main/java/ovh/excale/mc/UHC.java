package ovh.excale.mc;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.excale.mc.uhc.Challenger;
import ovh.excale.mc.uhc.Team;
import ovh.excale.mc.uhc.WorldManager;
import ovh.excale.mc.uhc.commands.TeamCommand;
import ovh.excale.mc.uhc.commands.TeamCommandExecutor;
import ovh.excale.mc.uhc.commands.UhcCommand;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UHC extends JavaPlugin {

	public static boolean DEBUG_MODE = false;

	public static UHC plugin() {
		return instance;
	}

	private static UHC instance;

	@Override
	public void onEnable() {
		super.onEnable();
		instance = this;

		// REMOVE ALL PREVIOUS VANILLA INSTANCES OF TEAMS ON RELOAD
		Team.clear();


		PlayerResponseListener responseListener = new PlayerResponseListener(this, 8);
		Bukkit.getPluginManager()
				.registerEvents(responseListener, this);

		LinkedHashMap<String, Argument> helpmepls = new LinkedHashMap<>();
		helpmepls.put("mode", new BooleanArgument());
		new CommandAPICommand("uhc-debug").withArguments(helpmepls)
				.executes((sender, args) -> {
					DEBUG_MODE = (boolean) args[0];
					sender.sendMessage("Debug Mode changed to " + DEBUG_MODE + "!");
				})
				.register();

		new CommandAPICommand("unbounds").executes((commandSender, objects) -> {
			commandSender.sendMessage("[" + Challenger.teamUnbound()
					.stream()
					.map(challenger -> challenger.vanilla()
							.getDisplayName())
					.collect(Collectors.joining(", ")) + "]");
		})
				.register();

		new CommandAPICommand("oceancheck").withPermission(CommandPermission.OP)
				.executesPlayer((player, objects) -> {
					World world = player.getWorld();
					Location location = player.getLocation();
					boolean isOcean = WorldManager.isOcean(world.getBiome((int) location.getX(), (int) location.getZ()));

					player.sendMessage("Ocean: " + isOcean);
				})
				.register();

		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("op", new UhcCommand.Argument().overrideSuggestions(UhcCommand.stringValues()));
		new CommandAPICommand("xkuhc").withPermission(CommandPermission.OP)
				.withArguments(arguments)
				.executesPlayer(new UhcCommand.Executor())
				.withAliases("uhc")
				.register();

		TeamCommandExecutor teamCommandExecutor = new TeamCommandExecutor();

		// TEAMS NO ARGS
		new CommandAPICommand("uhc-team").withPermission(CommandPermission.OP)
				.executesPlayer(teamCommandExecutor)
				.withAliases("teams")
				.register();

		arguments = new LinkedHashMap<>();
		arguments.put("name",
				new CustomArgument<>(Team::of).overrideSuggestions(commandSender -> Team.getAll()
						.stream()
						.map(Team::getName)
						.toArray(String[]::new)));

		// TEAMS 1 ARGS
		new CommandAPICommand("uhc-team").withPermission(CommandPermission.OP)
				.withArguments(new LinkedHashMap<>(arguments))
				.executesPlayer(teamCommandExecutor)
				.withAliases("teams")
				.register();

		arguments.put("op", new TeamCommand.Argument().overrideSuggestions("add", "remove", "list", "delete"));

		// TEAMS 2 ARGS
		new CommandAPICommand("uhc-team").withPermission(CommandPermission.OP)
				.withArguments(new LinkedHashMap<>(arguments))
				.executesPlayer(teamCommandExecutor)
				.withAliases("teams")
				.register();

		arguments.put("op", new TeamCommand.Argument().overrideSuggestions("add", "remove"));
		arguments.put("target", new PlayerArgument());

		// TEAMS 3 ARGS
		new CommandAPICommand("uhc-team").withPermission(CommandPermission.OP)
				.withArguments(new LinkedHashMap<>(arguments))
				.executesPlayer(teamCommandExecutor)
				.withAliases("teams")
				.register();


		new CommandAPICommand("xkuhcnewteam").executesPlayer((player, objects) -> {
			player.sendMessage("Insert the new team's name.");
			responseListener.await(player, (player1, message) -> {

				if(Pattern.matches("^[\\w\\[\\]\\-#@.]+$", message))
					player.performCommand("uhc-team " + message);
				else
					player.sendMessage(ChatColor.RED + "Illegal characters in name. Only alphanumeric characters, square brackets, dots, hashes and @s are permitted.");

			});
		})
				.register();

		arguments = new LinkedHashMap<>();
		arguments.put("team", new TeamCommand.Argument());
		arguments.put("color", new ChatColorArgument());
		new CommandAPICommand("teamcolor").withArguments(arguments)
				.withPermission(CommandPermission.OP)
				.executes((commandSender, objects) -> {
					Team team = (Team) objects[0];
					ChatColor color = (ChatColor) objects[1];
					team.setColor(color);

					commandSender.spigot()
							.sendMessage(new MenuBuilder(team.getName() + ": color").info("Changed color to " + color.name())
									.build());

				})
				.register();

		new CommandAPICommand("teamcheck").executesPlayer((player, objects) -> {
			Challenger challenger = Challenger.of(player);
			Team team = challenger.getTeam();
			if(team != null)
				player.sendMessage("You're in team " + team.getName() + ".");
			else
				player.sendMessage("You're not in a team.");
		})
				.register();

	}

}
