package ovh.excale.mc.uhc.commands;

import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ovh.excale.mc.MenuBuilder;
import ovh.excale.mc.uhc.Team;

import java.util.HashSet;
import java.util.Set;

public class TeamCommandExecutor implements PlayerCommandExecutor {

	private final static TextComponent BACK;
	private final static TextComponent NEW_TEAM;
	private final static HoverEvent ADD_PLAYER_HOVER;
	private final static HoverEvent REMOVE_PLAYER_HOVER;
	private final static HoverEvent COLOR_HOVER;
	private final static HoverEvent LIST_HOVER;
	private final static HoverEvent DELETE_HOVER;

	static {

		BACK = new TextComponent("BACK");
		BACK.setUnderlined(true);
		BACK.setBold(true);
		BACK.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uhc-team"));
		BACK.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {
				new TextComponent("Go back")
		}));

		NEW_TEAM = new TextComponent("NEW TEAM");
		NEW_TEAM.setUnderlined(true);
		NEW_TEAM.setBold(true);
		NEW_TEAM.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/xkuhcnewteam"));
		NEW_TEAM.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {
				new TextComponent("Create a new team")
		}));

		ADD_PLAYER_HOVER = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {
				new TextComponent("Add a player in the team")
		});
		REMOVE_PLAYER_HOVER = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {
				new TextComponent("Remove a player from the team")
		});
		COLOR_HOVER = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {
				new TextComponent("Set the color of the team")
		});
		LIST_HOVER = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {
				new TextComponent("List all the players in the team")
		});
		DELETE_HOVER = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {
				new TextComponent("Delete the team")
		});
	}

	@Override
	public void run(Player player, Object[] objects) {
		TextComponent backText = BACK;
		MenuBuilder menuBuilder;
		TeamCommand op;
		String teamName;
		Team team;

		switch(objects.length) {

			// MAIN MENU
			case 0:
				Set<Team> teams = Team.getAll();
				menuBuilder = new MenuBuilder("Teams").last(NEW_TEAM);

				for(Team team1 : teams) {
					teamName = team1.getName();
					ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uhc-team " + teamName);
					HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							new ComponentBuilder("Edit team ").append(teamName, FormatRetention.NONE).bold(true).create());

					menuBuilder.insert(teamName, hover, click);
				}

				player.spigot().sendMessage(menuBuilder.build());
				break;

			// TEAMS LIST
			case 1:
				team = (Team) objects[0];
				teamName = team.getName();
				menuBuilder = new MenuBuilder("Team: " + teamName);
				menuBuilder.insert("Add player",
						ADD_PLAYER_HOVER,
						new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uhc-team " + teamName + " add"))
						.insert("Remove player",
								REMOVE_PLAYER_HOVER,
								new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uhc-team " + teamName + " remove"))
						.insert("Color", COLOR_HOVER, new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uhc-team " + teamName + " color"))
						.insert("List", LIST_HOVER, new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uhc-team " + teamName + " list"))
						.insert("Delete", DELETE_HOVER, new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uhc-team " + teamName + " delete"))
						.last(backText);

				player.spigot().sendMessage(menuBuilder.build());
				break;

			// TEAM MANAGEMENT
			case 2:
				team = (Team) objects[0];
				op = (TeamCommand) objects[1];
				teamName = team.getName();
				menuBuilder = new MenuBuilder("Team: " + teamName);

				switch(op) {

					case LIST:
						backText = new TextComponent(backText);
						backText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uhc-team " + teamName));

						menuBuilder.last(backText);
						for(Player player1 : team.players())
							menuBuilder.insert(player1.getDisplayName(), null, null);
						break;

					case DELETE:
						team.unregister();
						menuBuilder.info("Team deleted.").last(backText);
						break;

					case ADD:
						Set<Player> players = new HashSet<>(Bukkit.getServer().getOnlinePlayers());
						players.removeAll(team.players());

						backText = new TextComponent(backText);
						backText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uhc-team " + teamName));

						menuBuilder.info("Choose the player you want to add:").last(backText);
						for(Player player1 : players) {
							String playerName = player1.getDisplayName();
							menuBuilder.insert(playerName,
									new HoverEvent(HoverEvent.Action.SHOW_TEXT,
											new ComponentBuilder("Add ").append(playerName, FormatRetention.NONE)
													.bold(true)
													.append(" to team ", FormatRetention.NONE)
													.append(teamName, FormatRetention.NONE)
													.bold(true)
													.create()),
									new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uhc-team " + teamName + " add " + playerName));
						}
						break;

					case REMOVE:
						backText = new TextComponent(BACK);
						backText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uhc-team " + teamName));

						menuBuilder.info("Choose the player you want to remove:").last(backText);
						for(Player player1 : team.players()) {
							String playerName = player1.getDisplayName();
							menuBuilder.insert(playerName,
									new HoverEvent(HoverEvent.Action.SHOW_TEXT,
											new ComponentBuilder("Remove ").append(playerName, FormatRetention.NONE)
													.bold(true)
													.append(" from team ", FormatRetention.NONE)
													.append(teamName, FormatRetention.NONE)
													.bold(true)
													.create()),
									new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uhc-team " + teamName + " remove " + playerName));
						}
						break;

					case COLOR:
						backText = new TextComponent(BACK);
						backText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uhc-team " + teamName));

						menuBuilder.info("Select the team color:")
								.insert("Black",
										null,
										new ClickEvent(ClickEvent.Action.RUN_COMMAND,
												"/teamcolor " + teamName + " " + ChatColor.BLACK.name()))
								.last(backText);
						break;

				}

				player.spigot().sendMessage(menuBuilder.build());
				break;

			// FULL TEAM OP
			case 3:
				team = (Team) objects[0];
				op = (TeamCommand) objects[1];
				Player target = (Player) objects[2];

				teamName = team.getName();
				backText = new TextComponent(BACK);
				backText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uhc-team " + teamName));
				menuBuilder = new MenuBuilder(teamName + ": " + op).last(backText);

				switch(op) {

					case ADD:
						if(team.add(target))
							menuBuilder.info("Added " + target.getDisplayName() + " to the team.");
						else
							menuBuilder.info("Failed to add " + target.getDisplayName() + " to the team.");
						break;

					case REMOVE:
						if(team.remove(target))
							menuBuilder.info("Removed " + target.getDisplayName() + " from the team.");
						else
							menuBuilder.info("Failed to remove " + target.getDisplayName() + " from the team.");
						break;

					default:
						menuBuilder.info("You shouldn't be here. Get lost.");
				}

				player.spigot().sendMessage(menuBuilder.build());
				break;

		}

	}

}
