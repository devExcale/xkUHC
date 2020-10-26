package ovh.excale.mc.uhc.exceptions;

import ovh.excale.mc.uhc.Game;

public class GameException extends Exception {

	private final Game game;

	public GameException(Game instance, String message) {
		super(message);
		game = instance;
	}

	public GameException(Game instance, Throwable cause) {
		super(cause);
		game = instance;
	}

	public Game getGameInstance() {
		return game;
	}

}
