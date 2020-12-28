package ovh.excale.mc.utils;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ovh.excale.mc.UHC;
import ovh.excale.mc.UhcGame;
import ovh.excale.mc.api.Game;
import ovh.excale.mc.api.Team;
import ovh.excale.mc.api.TeamedGame;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class Commands {

	public static final CommandAPICommand TEAMS = new CommandAPICommand("teams").withAliases("team", "t");
	private static final String[] TEAMS_ALIASES = new String[] { "team", "t" };

	public static final CommandAPICommand XKUHC = new CommandAPICommand("uhc").withAliases("xkuhc");
	private static final String[] UHC_ALIASES = new String[] { "xkuhc" };

	public static final ChatColor[] COLORS = Arrays.stream(ChatColor.values())
			.filter(ChatColor::isColor)
			.toArray(ChatColor[]::new);

	// TEAM CUSTOM_ARGUMENT
	private static Argument teamArgument() {
		UHC plugin = (UHC) UHC.plugin();

		return new CustomArgument<>("teamName", name -> {

			TeamedGame game = plugin.getGame();
			Team team;

			if(game == null)
				throw new CustomArgument.CustomArgumentException("There's no game instance");

			team = game.getTeam(name);
			if(team == null)
				throw new CustomArgument.CustomArgumentException("There's no such team");

			return team;

		}).overrideSuggestions(commandSender -> {

			TeamedGame game = plugin.getGame();
			String[] suggestions;

			if(game == null)
				suggestions = new String[0];
			else
				suggestions = game.getTeams()
						.stream()
						.map(Team::getName)
						.toArray(String[]::new);

			return suggestions;
		});
	}

	// TEAMS COMMAND BRANCH
	static {

		List<Argument> teamPlayerArguments = new LinkedList<>();
		teamPlayerArguments.add(Commands.teamArgument());
		teamPlayerArguments.add(new EntitySelectorArgument("playerName", EntitySelectorArgument.EntitySelector.ONE_PLAYER));

		TEAMS.withAliases(TEAMS_ALIASES)
				.withPermission(CommandPermission.OP)
				// ONLY GAME ADMIN CAN EXECUTE COMMAND
				.withRequirement(commandSender -> {

					UHC plugin = (UHC) UHC.plugin();
					Game game = plugin.getGame();
					boolean b = game == null;

					if(!b)
						b = ((Player) commandSender).getUniqueId()
								.equals(game.getAdminId());

					return b;
				})
				// LIST TEAMS
				.executes((commandSender, objects) -> {

					UHC plugin = (UHC) UHC.plugin();
					TeamedGame game = plugin.getGame();

					if(game == null)
						CommandAPI.fail("There's no game session.");

					StringBuilder sb = new StringBuilder("[TEAMS]");
					game.getTeams()
							.forEach(team -> {
								// -- TEAMNAME --
								sb.append("\n -- ")
										.append(team.getName())
										.append(" --");
								team.getChallengers()
										.forEach(challenger -> {
											// - PLAYERNAME [(OFFLINE)]
											sb.append("\n - ")
													.append(challenger.vanilla()
															.getDisplayName());
											if(!challenger.isOnline())
												sb.append(" (OFFLINE)");
										});
							});

					commandSender.sendMessage(sb.toString());

				})
				// CREATE TEAM
				.withSubcommand(new CommandAPICommand("create").withAliases("register")
						.withArguments(new StringArgument("teamName"))
						.executesPlayer((player, args) -> {

							UHC plugin = (UHC) UHC.plugin();
							TeamedGame game = plugin.getGame();
							String teamName = (String) args[0];

							if(game != null) {

								if(!Pattern.matches("^[\\w\\[\\]\\-#@.]+$", teamName))
									CommandAPI.fail("Illegal characters in team name.");

								Team team = game.createTeam(teamName);
								player.sendMessage(String.format("Created team %s%s%s", ChatColor.BOLD, team.getName(), ChatColor.RESET));

							} else
								CommandAPI.fail("You have to create a game session first.");

						}))
				// DELETE TEAM
				.withSubcommand(new CommandAPICommand("delete").withAliases("unregister")
						.withArguments(Commands.teamArgument())
						.executesPlayer((player, args) -> {

							Team team = (Team) args[0];
							String teamName = team.getName();

							if(team.getGame()
									.unregisterTeam(teamName))
								player.sendMessage(String.format("Team %s unregistered successfully.", teamName));
							else
								CommandAPI.fail("Failed to unregister the team.");

						}))
				// LIST TEAM MEMBERS
				.withSubcommand(new CommandAPICommand("list").withArguments(Commands.teamArgument())
						.executesPlayer((player, args) -> {

							Team team = (Team) args[0];
							StringBuilder sb = new StringBuilder("[" + team.getName() + "]");

							for(Player member : team.getMembers())
								sb.append("\n" + " - ")
										.append(member.getDisplayName());

							player.sendMessage(sb.toString());

						}))
				// ADD PLAYER TO TEAM
				// FIXME: CAN'T ADD PLAYER TO TEAM
				.withSubcommand(new CommandAPICommand("add").withArguments(teamPlayerArguments)
						.executesPlayer((player, args) -> {

							Team team = (Team) args[0];
							Player target = (Player) args[1];
							TeamedGame game = team.getGame();

							UUID adminId = game.getAdminId();
							if(!adminId.equals(player.getUniqueId()))
								CommandAPI.fail("You're not the admin of this game session.");

							if(team.add(target))
								player.sendMessage(String.format("Successfully added %s to team %s.",
										target.getDisplayName(),
										team.getName()));
							else
								CommandAPI.fail(String.format("Failed to add %s to team %s.", player.getDisplayName(), team.getName()));

						}))
				// REMOVE PLAYER FROM TEAM
				.withSubcommand(new CommandAPICommand("remove").withArguments(teamPlayerArguments)
						.executesPlayer((player, args) -> {

							Team team = (Team) args[0];
							Player target = (Player) args[1];
							TeamedGame game = team.getGame();

							UUID adminId = game.getAdminId();
							if(!adminId.equals(player.getUniqueId()))
								CommandAPI.fail("You're not the admin of this game session.");

							if(team.remove(target))
								player.sendMessage(String.format("Successfully removed %s from team %s.",
										team.getName(),
										player.getDisplayName()));
							else
								CommandAPI.fail(String.format("Failed to remove %s from team %s.",
										team.getName(),
										player.getDisplayName()));

						}))
				// SET TEAM COLOR
				.withSubcommand(new CommandAPICommand("color").withArguments(Commands.teamArgument(),
						new ChatColorArgument("color").safeOverrideSuggestions(COLORS))
						.executesPlayer((player, args) -> {

							Team team = (Team) args[0];
							ChatColor color = (ChatColor) args[1];

							if(color.isFormat())
								// TODO: FAIL MESSAGE
								CommandAPI.fail("TODO: FAIL MESSAGE");

							try {
								team.setColor(color);
							} catch(IllegalStateException e) {
								CommandAPI.fail(e.getMessage());
							}

						}))
				// GET TEAM COLOR
				.withSubcommand(new CommandAPICommand("color").executesPlayer((player, args) -> {

					UHC plugin = (UHC) UHC.plugin();
					TeamedGame game = plugin.getGame();

					if(game != null) {

						String teamName = (String) args[0];
						Team team = game.getTeam(teamName);

						if(team == null)
							CommandAPI.fail("There's no such team.");

						String color = team.getColor()
								.name();

						player.sendMessage(String.format("[%s]: %s", teamName, color));

					} else
						CommandAPI.fail("You have to create a game session first.");
				}));

	}

	// UHC COMMAND BRANCH
	static {

		XKUHC.withAliases(UHC_ALIASES)
				.withPermission(CommandPermission.OP)
				// ONLY GAME ADMIN CAN EXECUTE COMMAND
				.withRequirement(commandSender -> {

					Game game = ((UHC) UHC.plugin()).getGame();
					boolean b = game == null;

					if(!b)
						b = ((Player) commandSender).getUniqueId()
								.equals(game.getAdminId());

					return b;
				})
				// GAME CREATE
				.withSubcommand(new CommandAPICommand("create").executesPlayer((player, args) -> {

					UHC plugin = ((UHC) UHC.plugin());
					Game game = plugin.getGame();

					if(game != null)
						if(game.getStatus() == Game.Status.RUNNING)
							CommandAPI.fail("A game is already running.");
						else
							game.reset();

					plugin.setGame(new UhcGame(player));
					player.sendMessage("Game created!");

				}))
				// GAME START
				.withSubcommand(new CommandAPICommand("start").executesPlayer((player, args) -> {

					Game game = ((UHC) UHC.plugin()).getGame();

					if(game != null)
						if(game.getStatus() == Game.Status.READY)
							game.start();
						else
							CommandAPI.fail("Couldn't start game.");
					else
						CommandAPI.fail("No game found.");

				}))
				// GAME STOP
				.withSubcommand(new CommandAPICommand("stop").executesPlayer((player, args) -> {

					Game game = ((UHC) UHC.plugin()).getGame();

					if(game != null)
						if(game.getStatus() == Game.Status.RUNNING)
							game.stop();
						else
							CommandAPI.fail("The game's not running.");
					else
						CommandAPI.fail("No game found.");

				}))
				// GAME RESET
				.withSubcommand(new CommandAPICommand("reset").executesPlayer((player, args) -> {

					Game game = ((UHC) UHC.plugin()).getGame();

					if(game != null)
						if(game.getStatus() != Game.Status.RUNNING)
							game.reset();
						else
							CommandAPI.fail("The game's running.");
					else
						CommandAPI.fail("No game found.");

				}))
				// DEBUG MODE CHECK
				.withSubcommand(new CommandAPICommand("debug").executesPlayer((player, args) -> {

					// TODO: PRINT DEBUG MODE
					CommandAPI.fail("WIP");

				}))
				// DEBUG MODE CHECK
				.withSubcommand(new CommandAPICommand("debug").withArguments(new BooleanArgument("value"))
						.executesPlayer((player, args) -> {

							// TODO: SET DEBUG MODE
							CommandAPI.fail("WIP");

						}))
				// CLEAN UP WORLDS
				.withSubcommand(new CommandAPICommand("clean").executesPlayer((Player player, Object[] args) -> RandomUhcWorldGenerator.purgeWorlds(
						worlds -> player.sendMessage("UHC CleanUp: removed " + worlds + " worlds."))));

	}

}
