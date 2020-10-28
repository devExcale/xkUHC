package ovh.excale.mc;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import ovh.excale.mc.uhc.Challenger;
import ovh.excale.mc.uhc.Game;
import ovh.excale.mc.uhc.Session;
import ovh.excale.mc.uhc.Team;
import ovh.excale.mc.uhc.commands.TeamCommand;
import ovh.excale.mc.uhc.commands.TeamCommandExecutor;
import ovh.excale.mc.uhc.commands.UhcCommand;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// TODO: CHECK PlayerDeathEvent LISTENER IN SESSION RUN
public class UHC extends JavaPlugin {

	public static boolean DEBUG_MODE = false;

	public static UHC plugin() {
		return instance;
	}

	private static UHC instance;

	private Game game;

	@Override
	public void onEnable() {
		super.onEnable();
		instance = this;

		//noinspection ConstantConditions
		Scoreboard scoreboard = Bukkit.getScoreboardManager()
				.getNewScoreboard();


		PlayerResponseListener responseListener = new PlayerResponseListener(this, 8);
		Bukkit.getPluginManager()
				.registerEvents(responseListener, this);

		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("mode", new BooleanArgument());
		new CommandAPICommand("uhc-debug").withArguments(arguments)
				.executes((sender, args) -> {
					DEBUG_MODE = (boolean) args[0];
					sender.sendMessage("Debug Mode changed to " + DEBUG_MODE + "!");
				})
				.register();

		new CommandAPICommand("unbounds").executes((commandSender, objects) -> {
			commandSender.sendMessage("[" + Challenger.teamUnbounds()
					.stream()
					.map(challenger -> challenger.vanilla()
							.getDisplayName())
					.collect(Collectors.joining(", ")) + "]");
		})
				.register();

		arguments = new LinkedHashMap<>();
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
		arguments.put("name", new StringArgument());

		// TEAMS 1 ARGS
		new CommandAPICommand("uhc-team").withPermission(CommandPermission.OP)
				.withArguments(new LinkedHashMap<>(arguments))
				.executesPlayer(teamCommandExecutor)
				.withAliases("teams")
				.register();

		arguments.put("op", new TeamCommand.Argument().overrideSuggestions(TeamCommand.stringValues()));

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
		arguments.put("team", new StringArgument());
		arguments.put("color", new ChatColorArgument());
		new CommandAPICommand("teamcolor").withArguments(arguments)
				.withPermission(CommandPermission.OP)
				.executesPlayer((player, objects) -> {
					Session session = Session.by(player);
					ChatColor color = (ChatColor) objects[1];

					if(session != null) {
						Team team = session.getTeamManager()
								.getTeam(((String) objects[0]));

						if(team != null) {
							team.setColor(color);

							final TextComponent BACK = new TextComponent("BACK");
							BACK.setUnderlined(true);
							BACK.setBold(true);
							BACK.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uhc-team " + team.getName()));
							BACK.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {
									new TextComponent("Go back")
							}));
							player.spigot()
									.sendMessage(new MenuBuilder(team.getName() + ": color").info("Changed color to " + color.name())
											.last(BACK)
											.build());

						}
					}
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
