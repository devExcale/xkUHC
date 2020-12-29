package ovh.excale.mc.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AChatColorArgument;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ovh.excale.mc.api.Challenger;
import ovh.excale.mc.api.Team;
import ovh.excale.mc.api.TeamedGame;

@Command("uhcteams")
public class TeamsCommand {

	// TODO: BYPASS ADMIN CHECK WITH CONSOLE

	@Default
	public static void listTeams(CommandSender sender) throws WrapperCommandSyntaxException {

		TeamedGame game = UhcCommand.getGame();

		if(game == null)
			CommandAPI.fail("No game found");

		StringBuilder sb = new StringBuilder("\n[TEAMS]");

		for(Team team : game.getTeams()) {
			// -- TEAMNAME --
			sb.append("\n -- ")
					.append(team.getName())
					.append(" --");

			for(Challenger challenger : team.getChallengers()) {
				// - PLAYERNAME [(OFFLINE)]
				sb.append("\n - ")
						.append(challenger.vanilla()
								.getDisplayName());
				if(!challenger.isOnline())
					sb.append(" (OFFLINE)");
			}
		}

		sender.sendMessage(sb.toString());

	}

	@Subcommand("list")
	public static void listTeamMembers(CommandSender sender, @AStringArgument String teamName) throws WrapperCommandSyntaxException {

		TeamedGame game = UhcCommand.getGame();
		if(game == null)
			CommandAPI.fail("No game found");

		Team team = game.getTeam(teamName);
		if(team == null)
			CommandAPI.fail("No such team");

		StringBuilder sb = new StringBuilder("\n[ + " + team.getName() + " + ]");
		for(Challenger challenger : team.getChallengers()) {

			Player player = challenger.vanilla();
			sb.append("\n - ")
					.append(player.getDisplayName());

			if(!challenger.isOnline())
				sb.append(" (OFFLINE)");

		}

		sender.sendMessage(sb.toString());

	}

	@Subcommand("create")
	public static void createTeam(Player sender, @AStringArgument String teamName) throws WrapperCommandSyntaxException {

		TeamedGame game = UhcCommand.getGame();
		if(game == null)
			CommandAPI.fail("No game found");

		UhcCommand.validateAdmin(game, sender);

		Team team = game.getTeam(teamName);

		if(team != null)
			CommandAPI.fail("This team is already registered");

		try {
			team = game.createTeam(teamName);
		} catch(IllegalStateException e) {
			CommandAPI.fail(e.getMessage());
		}

		sender.sendMessage("Created team " + team.getName());

	}

	@Subcommand("delete")
	public static void deleteTeam(Player sender, @AStringArgument String teamName) throws WrapperCommandSyntaxException {

		TeamedGame game = UhcCommand.getGame();
		if(game == null)
			CommandAPI.fail("No game found");

		UhcCommand.validateAdmin(game, sender);

		Team team = game.getTeam(teamName);

		if(team == null)
			CommandAPI.fail("No such team");

		try {
			// TODO: THIS IS HIDEOUS, I SHOULD JUST USE Team::unregister
			if(!game.unregisterTeam(teamName))
				CommandAPI.fail("Couldn't unregister team");
		} catch(IllegalStateException e) {
			CommandAPI.fail(e.getMessage());
		}

		sender.sendMessage("Feleted team " + team.getName());

	}

	@Subcommand("add")
	public static void addPlayerToTeam(Player sender, @AStringArgument String teamName, @APlayerArgument Player target) throws WrapperCommandSyntaxException {

		TeamedGame game = UhcCommand.getGame();
		if(game == null)
			CommandAPI.fail("No game found");

		UhcCommand.validateAdmin(game, sender);

		Team team = game.getTeam(teamName);

		if(team == null)
			CommandAPI.fail("No such team");

		try {
			if(!team.add(target))
				CommandAPI.fail("Couldn't add player to team");
		} catch(IllegalStateException e) {
			CommandAPI.fail(e.getMessage());
		}

		sender.sendMessage("Added " + target.getDisplayName() + " to team " + team.getName());

	}

	@Subcommand("remove")
	public static void removePlayerFromTeam(Player sender, @AStringArgument String teamName, @APlayerArgument Player target) throws WrapperCommandSyntaxException {

		TeamedGame game = UhcCommand.getGame();
		if(game == null)
			CommandAPI.fail("No game found");

		UhcCommand.validateAdmin(game, sender);

		Team team = game.getTeam(teamName);

		if(team == null)
			CommandAPI.fail("No such team");

		try {
			if(!team.remove(target))
				CommandAPI.fail("Couldn't remove player from team");
		} catch(IllegalStateException e) {
			CommandAPI.fail(e.getMessage());
		}

		sender.sendMessage("Removed " + target.getDisplayName() + " from team " + team.getName());

	}

	@Subcommand("color")
	public static void getColor(CommandSender sender, @AStringArgument String teamName) throws WrapperCommandSyntaxException {

		TeamedGame game = UhcCommand.getGame();
		if(game == null)
			CommandAPI.fail("No game found");

		Team team = game.getTeam(teamName);

		if(team == null)
			CommandAPI.fail("No such team");

		ChatColor color = team.getColor();

		sender.sendMessage("[" + team.getName() + "] " + color + color.name());

	}

	@Subcommand("color")
	public static void setColor(CommandSender sender, @AStringArgument String teamName, @AChatColorArgument ChatColor color) throws WrapperCommandSyntaxException {

		TeamedGame game = UhcCommand.getGame();
		if(game == null)
			CommandAPI.fail("No game found");

		Team team = game.getTeam(teamName);

		if(team == null)
			CommandAPI.fail("No such team");

		try {
			team.setColor(color);
		} catch(IllegalStateException e) {
			CommandAPI.fail(e.getMessage());
		}

		sender.sendMessage("Set team " + ChatColor.BOLD + team.getName() + ChatColor.RESET + " color to " + color + color.name());

	}

}
