package ovh.excale.mc.uhc.commands;

import dev.jorel.commandapi.arguments.CustomArgument;

import java.util.stream.Stream;

@SuppressWarnings("unused")
public enum TeamCommand {

	ADD,
	REMOVE,
	DELETE,
	LIST,
	COLOR;

	public static String[] stringValues() {
		return Stream.of(values())
				.map(Enum::toString)
				.map(String::toLowerCase)
				.toArray(String[]::new);
	}

	public static class Argument extends CustomArgument<TeamCommand> {

		public Argument() {
			super(string -> {
				try {
					return TeamCommand.valueOf(string.toUpperCase());
				} catch(IllegalArgumentException e) {
					throw new CustomArgumentException(new MessageBuilder("Unknown op: ").appendArgInput()
							.appendHere());
				}
			});
		}

	}

}
