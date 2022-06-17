package ovh.excale.xkuhc.configuration;

import org.jetbrains.annotations.Nullable;

import static ovh.excale.xkuhc.configuration.BorderAction.ActionType.MOVE;

@SuppressWarnings("ClassCanBeRecord")
public class BorderAction {

	private final ActionType actionType;
	private final int time;
	private final int borderSize;

	protected BorderAction(ActionType actionType, int time, int borderSize) throws IllegalArgumentException {

		if(time < 1)
			throw new IllegalArgumentException("Expected positive integer parameter time, received: " + time);

		if(actionType == MOVE && borderSize < 2)
			throw new IllegalArgumentException("Expected integer parameter borderSize > 2, received: " + borderSize);

		this.actionType = actionType;
		this.time = time;
		this.borderSize = (actionType == MOVE) ? borderSize : 0;

	}

	public int getTime() {
		return time;
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

		public String getMessageKeyShort() {
			return msgKey + ".short";
		}

		public String getMessageKeyLong() {
			return msgKey + ".long";
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
