package ovh.excale.mc.commands;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import ovh.excale.mc.Session;
import ovh.excale.mc.UHC;

import java.util.Optional;
import java.util.stream.Stream;

public enum UhcCommand {

	CREATE,
	GENERATE,
	START,
	STOP,
	PURGE,
	CLEANUP,
	DEBUG;

	public static String[] stringValues() {
		return Stream.of(values())
				.map(Enum::toString)
				.map(String::toLowerCase)
				.toArray(String[]::new);
	}

	public static class Executor implements PlayerCommandExecutor {

		@SuppressWarnings("RedundantThrows")
		@Override
		public void run(Player player, Object[] objects) throws WrapperCommandSyntaxException {
			UhcCommand op = (UhcCommand) objects[0];
			Session session = Session.by(player);

			switch(op) {

				case CREATE:
					if(session != null)
						session.purge();

					Session.create(player);
					player.sendMessage("Session created!");
					break;

				case PURGE:
					if(session == null)
						player.sendMessage("You don't have a UHC Session on.");

					else
						session.purge();

					break;

				case START:
					if(session == null)
						player.sendMessage("Create a UHC Session first.");
					else
						session.start();

					Optional.ofNullable(Bukkit.getScoreboardManager())
							.ifPresent(scoreboardManager -> scoreboardManager.getMainScoreboard()
									.getObjectives()
									.forEach(objective -> player.sendMessage(objective.getDisplayName())));
					break;

				case STOP:
					break;

				case GENERATE:
					if(session == null)
						player.sendMessage("Create a UHC Session first.");

					else {
						BukkitScheduler scheduler = Bukkit.getScheduler();

						scheduler.runTaskAsynchronously(UHC.plugin(), () -> {
							player.sendMessage("Generating world...");

							Optional<World> world = session.generateWorld();

							if(world.isPresent())
								player.sendMessage("World generated!");
							else
								player.sendMessage("Couldn't generate world.");
						});
					}
					break;

				case CLEANUP:
//					WorldManager.cleanUpWorlds();
					break;

				case DEBUG:
					if(session == null)
						player.sendMessage("Create a UHC Session first.");
					else
						player.sendMessage(session.debug());
					break;

			}

		}

	}

//	public static class Argument extends CustomArgument<UhcCommand> {
//
//		public Argument() {
//			super(string -> {
//				try {
//					return UhcCommand.valueOf(string.toUpperCase());
//				} catch(IllegalArgumentException e) {
//					throw new CustomArgumentException(new MessageBuilder("Unknown op: ").appendArgInput()
//							.appendHere());
//				}
//			});
//		}
//
//	}

}
