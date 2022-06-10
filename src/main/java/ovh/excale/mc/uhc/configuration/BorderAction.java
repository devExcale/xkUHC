package ovh.excale.mc.uhc.configuration;

import org.jetbrains.annotations.Nullable;

import static ovh.excale.mc.uhc.configuration.BorderAction.ActionType.MOVE;

public class BorderAction {

	private final ActionType actionType;
	private final int minutes;
	private final int borderSize;

	protected BorderAction(ActionType actionType, int seconds, int borderSize) throws IllegalArgumentException {

		if(seconds < 1)
			throw new IllegalArgumentException("Expected positive integer parameter seconds, received: " + seconds);

		if(actionType == MOVE && borderSize < 2)
			throw new IllegalArgumentException("Expected integer parameter borderSize > 2, received: " + borderSize);

		this.actionType = actionType;
		this.minutes = seconds;
		this.borderSize = (actionType == MOVE) ? borderSize : 0;

	}

	public int getMinutes() {
		return minutes;
	}

	public int getBorderSize() {
		return borderSize;
	}

	public ActionType getType() {
		return actionType;
	}

	public enum ActionType {

		HOLD("border.hold"),
		MOVE("border.move");

		private final String msgKey;

		ActionType(String msgKey) {
			this.msgKey = msgKey;
		}

		public String getMessageKey() {
			return msgKey;
		}

		public static @Nullable ActionType parse(String value) {

			ActionType type = null;
			try {

				type = ActionType.valueOf(value.toUpperCase());

			} catch(Exception ignored) {
			}

			return type;
		}

	}

}
