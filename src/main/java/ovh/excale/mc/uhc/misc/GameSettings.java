package ovh.excale.mc.uhc.misc;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import ovh.excale.mc.UHC;
import ovh.excale.mc.uhc.misc.BorderAction.ActionType;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameSettings {

	private static final int MIN_STARTING_BORDER_SIZE = 500;

	private static final Logger logger = UHC.logger();

	public static GameSettings fromConfig() {

		Plugin plugin = UHC.plugin();
		plugin.reloadConfig();

		ConfigurationSection config = plugin.getConfig();
		GameSettings settings = new GameSettings();

		settings.enableFriendlyFire = config.getBoolean("uhc.FriendlyFire", false);
		settings.lethalDisconnectTime = config.getInt("uhc.LethalDisconnectTime", Integer.MIN_VALUE);

		try {

			settings.initialSize = config.getInt("uhc.border.InitialSize", Integer.MIN_VALUE);
			if(settings.initialSize == Integer.MIN_VALUE)
				throw new IllegalArgumentException("Missing parameter uhc.border.InitialSize");
			if(settings.initialSize < MIN_STARTING_BORDER_SIZE)
				throw new IllegalArgumentException("InitialSize is too small!");

			List<Map<?, ?>> actions = config.getMapList("uhc.border.actions");
			actions.forEach(map -> map.forEach((o, o2) -> System.out.println(o.toString() + " : " + o2.toString())));
			for(int i = 0; i < actions.size(); i++) {

				//noinspection unchecked
				Map<String, Object> map = (Map<String, Object>) actions.get(i);
				String typeString = map.get("Action")
						.toString();

				ActionType type = ActionType.parse(typeString);
				if(type == null)
					throw new IllegalArgumentException("Missing parameter Action @ uhc.border.actions[" + i + "]");

				int borderSize = (int) map.getOrDefault("BorderSize", Integer.MIN_VALUE);
				if(borderSize == Integer.MIN_VALUE)
					throw new IllegalArgumentException("Missing parameter BorderSize @ uhc.border.actions[" + i + "]");

				int minutes = (int) map.getOrDefault("Minutes", Integer.MIN_VALUE);
				if(minutes == Integer.MIN_VALUE)
					throw new IllegalArgumentException("Missing parameter Minutes @ uhc.border.actions[" + i + "]");
				if(minutes < 1)
					throw new IllegalArgumentException("Parameter Minutes must be positive @ uhc.border.actions[" + i + "]");

				settings.borderActions.add(new BorderAction(minutes, borderSize, type));

			}

		} catch(IllegalArgumentException e) {
			settings.borderActions.clear();
			settings.errorMessage = e.getMessage();
			logger.log(Level.SEVERE, e.getMessage(), e);
		}

		return settings;
	}

	private final List<BorderAction> borderActions;
	private boolean enableFriendlyFire;
	private int initialSize;
	private int lethalDisconnectTime;
	private String errorMessage;

	private GameSettings() {
		borderActions = new LinkedList<>();
		enableFriendlyFire = false;
		initialSize = 2000;
		lethalDisconnectTime = -1;
		errorMessage = "";
	}

	public boolean isLegal() {
		return !borderActions.isEmpty() && initialSize >= MIN_STARTING_BORDER_SIZE;
	}

	public boolean isFriendlyFireEnabled() {
		return enableFriendlyFire;
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
