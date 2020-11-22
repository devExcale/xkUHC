package ovh.excale.mc;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ovh.excale.mc.api.Game;
import ovh.excale.mc.api.Team;
import ovh.excale.mc.api.TeamedGame;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class Commands {

	public static final CommandAPICommand TEAMS = new CommandAPICommand("teams");

	public static final CommandAPICommand XKUHC = new CommandAPICommand("uhc").withAliases("xkuhc");

	// TEAMS COMMAND BRANCH
	static {

		List<Argument> teamPlayerArguments = new LinkedList<>();
		teamPlayerArguments.add(new StringArgument("teamName"));
		teamPlayerArguments.add(new EntitySelectorArgument("playerName", EntitySelectorArgument.EntitySelector.ONE_PLAYER));

		TEAMS.withPermission(CommandPermission.OP)
				// ONLY GAME ADMIN CAN EXECUTE COMMAND
				.withRequirement(commandSender -> {

					Game game = ((UHC) UHC.plugin()).getGame();
					boolean b = game != null;

					if(b)
						b = ((Player) commandSender).getUniqueId()
								.equals(game.getAdminId());

					return b;
				})
				// CREATE TEAM
				.withSubcommand(new CommandAPICommand("create").withAliases("register")
						.withArguments(new StringArgument("teamName"))
						.executesPlayer((player, args) -> {

							TeamedGame game = ((UHC) UHC.plugin()).getGame();
							String teamName = ((String) args[0]);

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
						.withArguments(new StringArgument("teamName"))
						.executesPlayer((player, args) -> {

							TeamedGame game = ((UHC) UHC.plugin()).getGame();
							String teamName = ((String) args[0]);

							if(game != null) {

								UUID adminId = game.getAdminId();
								if(!adminId.equals(player.getUniqueId()))
									CommandAPI.fail("You're not the admin of this game session.");

								if(game.unregisterTeam(teamName))
									player.sendMessage(String.format("Team %s unregistered successfully.", teamName));
								else
									CommandAPI.fail("Failed to unregister the team.");

							} else
								CommandAPI.fail("You have to create a game session first.");

						}))
				// LIST TEAM MEMBERS
				.withSubcommand(new CommandAPICommand("list").withArguments(new StringArgument("teamName"))
						.executesPlayer((player, args) -> {

							TeamedGame game = ((UHC) UHC.plugin()).getGame();

							if(game != null) {

								Team team = game.createTeam((String) args[0]);

								if(team == null)
									CommandAPI.fail("There's no such team.");

								StringBuilder sb = new StringBuilder("[" + team.getName() + "]");

								for(Player member : team.getMembers())
									sb.append("\n" + " - ")
											.append(member.getDisplayName());

								player.sendMessage(sb.toString());

							} else
								CommandAPI.fail("You have to create a game session first.");

						}))
				// ADD PLAYER TO TEAM
				.withSubcommand(new CommandAPICommand("add").withArguments(teamPlayerArguments)
						.executesPlayer((player, args) -> {

							TeamedGame game = ((UHC) UHC.plugin()).getGame();
							String teamName = ((String) args[0]);
							Player target = ((Player) args[1]);

							if(game != null) {

								UUID adminId = game.getAdminId();
								if(!adminId.equals(player.getUniqueId()))
									CommandAPI.fail("You're not the admin of this game session.");

								Team team = game.getTeam(teamName);

								if(team == null)
									CommandAPI.fail("There's no such team.");

								if(team.add(target))
									player.sendMessage(String.format("Successfully added %s to team %s.",
											teamName,
											player.getDisplayName()));
								else
									CommandAPI.fail(String.format("Failed to add %s to team %s.", teamName, player.getDisplayName()));
							}

						}))
				// REMOVE PLAYER FROM TEAM
				.withSubcommand(new CommandAPICommand("remove").withArguments(teamPlayerArguments)
						.executesPlayer((player, args) -> {

							TeamedGame game = ((UHC) UHC.plugin()).getGame();
							String teamName = ((String) args[0]);
							Player target = ((Player) args[1]);

							if(game != null) {

								UUID adminId = game.getAdminId();
								if(!adminId.equals(player.getUniqueId()))
									CommandAPI.fail("You're not the admin of this game session.");

								Team team = game.getTeam(teamName);

								if(team == null)
									CommandAPI.fail("There's no such team.");

								if(team.remove(target))
									player.sendMessage(String.format("Successfully removed %s from team %s.",
											teamName,
											player.getDisplayName()));
								else
									CommandAPI.fail(String.format("Failed to remove %s from team %s.", teamName, player.getDisplayName()));
							}

						}))
				// GET TEAM COLOR
				.withSubcommand(new CommandAPICommand("color").executesPlayer((player, args) -> {

					TeamedGame game = ((UHC) UHC.plugin()).getGame();

					if(game != null) {

						String teamName = ((String) args[0]);
						Team team = game.getTeam(teamName);

						if(team == null)
							CommandAPI.fail("There's no such team.");

						String color = team.getColor()
								.name();

						player.sendMessage("[" + team.getName() + "]: " + color);

					} else
						CommandAPI.fail("You have to create a game session first.");
				}));

	}

	// UHC COMMAND BRANCH
	static {

		XKUHC.withPermission(CommandPermission.OP)
				// ONLY GAME ADMIN CAN EXECUTE COMMAND
				.withRequirement(commandSender -> {

					Game game = ((UHC) UHC.plugin()).getGame();
					boolean b = game != null;

					if(b)
						b = ((Player) commandSender).getUniqueId()
								.equals(game.getAdminId());

					return b;
				})
				// GAME CREATE
				.withSubcommand(new CommandAPICommand("create").executesPlayer((player, args) -> {

					UHC plugin = ((UHC) UHC.plugin());
					Game game = plugin.getGame();

					if(game != null)
						if(game.getState() == Game.State.RUNNING)
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
						if(game.getState() == Game.State.READY)
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
						if(game.getState() == Game.State.RUNNING)
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
						if(game.getState() != Game.State.RUNNING)
							game.reset();
						else
							CommandAPI.fail("The game's running.");
					else
						CommandAPI.fail("No game found.");

				}))
				// GAME PREPARE
				.withSubcommand(new CommandAPICommand("prepare").executesPlayer((player, args) -> {

					// TODO: GAME PREPARE (WORLD GEN)
					CommandAPI.fail("WIP");

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
				.withSubcommand(new CommandAPICommand("clean").executesPlayer((player, args) -> {

					// TODO: CLEAN UP WORLDS
					CommandAPI.fail("WIP");

				}));

	}

}
