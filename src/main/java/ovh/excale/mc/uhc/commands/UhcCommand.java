package ovh.excale.mc.uhc.commands;

import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import ovh.excale.mc.UHC;
import ovh.excale.mc.uhc.Session;

import java.util.Optional;
import java.util.stream.Stream;

public enum UhcCommand {

	CREATE,
	GENERATE,
	START,
	STOP,
	FORGET,
	CLEANUP;

	public static String[] stringValues() {
		return Stream.of(values())
				.map(Enum::toString)
				.map(String::toLowerCase)
				.toArray(String[]::new);
	}

	public static class Executor implements PlayerCommandExecutor {

		@Override
		public void run(Player player, Object[] objects) throws WrapperCommandSyntaxException {
			UhcCommand op = (UhcCommand) objects[0];

			switch(op) {

				case CREATE:
					Session.by(player);
					break;

				case FORGET:
					break;

				case START:
					Optional.ofNullable(Bukkit.getScoreboardManager())
							.ifPresent(scoreboardManager -> scoreboardManager.getMainScoreboard()
									.getObjectives()
									.forEach(Objective::getDisplayName));
					break;

				case STOP:
					break;

				case GENERATE:
					player.sendMessage("Generating world...");
					Bukkit.getScheduler()
							.runTaskAsynchronously(UHC.plugin(), () -> {
								Optional<World> world = Session.by(player)
										.generateWorld();
								if(world.isPresent()) {
									player.sendMessage("World generated! Teleporting.");
									player.teleport(world.get()
											.getSpawnLocation());
									player.sendMessage("This world's id is " + world.get()
											.getName()
											.split("\\.")[0]);
								}
							});
					break;

				case CLEANUP:
					break;

			}

		}

	}

	public static class Argument extends CustomArgument<UhcCommand> {

		public Argument() {
			super(string -> {
				try {
					return UhcCommand.valueOf(string.toUpperCase());
				} catch(IllegalArgumentException e) {
					throw new CustomArgumentException(new MessageBuilder("Unknown op: ").appendArgInput()
							.appendHere());
				}
			});
		}

	}

}
