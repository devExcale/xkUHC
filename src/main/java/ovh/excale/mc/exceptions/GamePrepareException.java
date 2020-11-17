package ovh.excale.mc.exceptions;

import ovh.excale.mc.Game;

public class GamePrepareException extends GameException {

	public GamePrepareException(Game instance, String message) {
		super(instance, message);
	}

	public GamePrepareException(Game instance, Throwable cause) {
		super(instance, cause);
	}

}
