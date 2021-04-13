package ovh.excale.mc.uhc.misc;

import org.jetbrains.annotations.Nullable;

public class BorderAction {

	private final int minutes;
	private final int borderSize;
	private final ActionType actionType;

	protected BorderAction(int minutes, int borderSize, ActionType actionType) throws IllegalArgumentException {

		if(minutes < 1)
			throw new IllegalArgumentException("Parameter Minutes must be positive");
		this.minutes = minutes;

		if(borderSize < 500)
			throw new IllegalArgumentException("Parameter BorderSize is too small");
		this.borderSize = borderSize;

		this.actionType = actionType;
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

		HOLD,
		SHRINK;

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
