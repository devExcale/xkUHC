package ovh.excale.mc.uhc.exceptions;

import ovh.excale.mc.uhc.Game;

public class GamePrepareException extends GameException {

	public GamePrepareException(Game instance, String message) {
		super(instance, message);
	}

	public GamePrepareException(Game instance, Throwable cause) {
		super(instance, cause);
	}

}
