package ovh.excale.xkuhc.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import ovh.excale.xkuhc.xkUHC;
import ovh.excale.xkuhc.configuration.BorderAction.ActionType;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Integer.MIN_VALUE;
import static ovh.excale.xkuhc.configuration.BorderAction.ActionType.MOVE;
import static ovh.excale.xkuhc.configuration.ConfigKeys.*;

public class GameSettings {

	public static GameSettings fromConfig() {

		Plugin plugin = xkUHC.instance();
		plugin.reloadConfig();

		Logger log = plugin.getLogger();

		ConfigurationSection config = plugin.getConfig();
		GameSettings settings = new GameSettings();

		settings.friendlyFire = config.getBoolean(FRIENDLY_FIRE, false);
		settings.repellentEnabled = config.getBoolean(REPELLENT, true);
		settings.resetAfter = config.getBoolean(RESET_AFTER, true);
		settings.lethalDisconnectTime = config.getInt(LETHAL_DISCONNECT, 0);

		if(settings.lethalDisconnectTime < 0) {

			log.warning("Invalid %s value, disabling it".formatted(LETHAL_DISCONNECT));
			settings.lethalDisconnectTime = 0;

		}

		settings.initialSize = config.getInt(BORDER_INITIAL_SIZE, MIN_VALUE);

		if(settings.initialSize == MIN_VALUE)
			throw new IllegalArgumentException("Missing parameter %s".formatted(BORDER_INITIAL_SIZE));

		if(settings.initialSize < 2)
			throw new IllegalArgumentException("Expected integer %s with value > 2, provided: %d".formatted(BORDER_INITIAL_SIZE, settings.initialSize));

		try {

			List<Map<?, ?>> actions = config.getMapList(BORDER_ACTIONS);

			// actions.forEach(map -> map.forEach((o, o2) -> System.out.println(o.toString() + " : " + o2.toString())));

			for(int i = 0; i < actions.size(); i++) {

				//noinspection unchecked
				Map<String, Object> map = (Map<String, Object>) actions.get(i);

				// Get ActionType
				Object typeObj = map.get(BORDER_ACTION_TYPE);
				ActionType type = ActionType.parse(typeObj.toString());

				if(type == null)
					throw new IllegalArgumentException("Missing parameter %s @ %s[%d]".formatted(BORDER_ACTION_TYPE, BORDER_ACTIONS, i));

				// Get BorderSize
				int borderSize = (int) map.getOrDefault(BORDER_ACTION_BORDER_SIZE, MIN_VALUE);

				if(type == MOVE && borderSize == MIN_VALUE)
					throw new IllegalArgumentException("Missing parameter %s @ %s[%d]".formatted(BORDER_ACTION_BORDER_SIZE, BORDER_ACTIONS, i));

				if(type == MOVE && borderSize < 2)
					throw new IllegalArgumentException(
							"Expected integer %s with value > 2, provided: %d @ %s[%d]".formatted(BORDER_ACTION_BORDER_SIZE, borderSize, BORDER_ACTIONS, i));

				// Get Time
				int time = (int) map.getOrDefault(BORDER_ACTION_TIME, MIN_VALUE);

				if(time == MIN_VALUE)
					throw new IllegalArgumentException("Missing parameter %s @ %s[%d]".formatted(BORDER_ACTION_TIME, BORDER_ACTIONS, i));

				if(time < 1)
					throw new IllegalArgumentException("Expected positive integer %s, provided: %d @ %s[%d]".formatted(BORDER_ACTION_TIME, time, BORDER_ACTIONS, i));

				// Finally
				settings.borderActions.add(new BorderAction(type, time, borderSize));

			}

		} catch(IllegalArgumentException e) {

			settings.borderActions.clear();
			settings.errorMessage = e.getMessage();

			log.log(Level.SEVERE, e.getMessage(), e);

		}

		return settings;
	}

	private String errorMessage;
	private final List<BorderAction> borderActions;

	private boolean friendlyFire;
	private boolean repellentEnabled;
	private boolean resetAfter;

	private int initialSize;
	private int lethalDisconnectTime;

	private GameSettings() {

		errorMessage = "";
		borderActions = new LinkedList<>();

		friendlyFire = false;
		repellentEnabled = true;
		resetAfter = true;

		initialSize = 1500;
		lethalDisconnectTime = 0;

	}

	public boolean isLegal() {
		return !borderActions.isEmpty();
	}

	public boolean isFriendlyFireEnabled() {
		return !friendlyFire;
	}

	public boolean isRepellentEnabled() {
		return repellentEnabled;
	}

	public boolean isResetAfter() {
		return resetAfter;
	}

	public Iterator<BorderAction> getBorderActionIterator() {
		return borderActions.listIterator();
	}

	public int getInitialBorderSize() {
		return initialSize;
	}

	public int getLethalDisconnectTime() {
		return lethalDisconnectTime;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
