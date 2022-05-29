package ovh.excale.mc.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class MessageBundles {

	private final ResourceBundle main;
	private final YamlConfiguration gameOriginal;
	private final YamlConfiguration gameUser;

	private final Logger log;
	private final Random random;

	public MessageBundles(@NotNull Plugin plugin) {

		main = ResourceBundle.getBundle("messages.main");
		log = plugin.getLogger();

		//noinspection ConstantConditions
		gameOriginal = YamlConfiguration.loadConfiguration(new InputStreamReader(getClass().getClassLoader()
				.getResourceAsStream("messages/game.yml")));

		InputStream resourceStream = plugin.getResource("messages/game.yml");
		gameUser = (resourceStream != null) ? YamlConfiguration.loadConfiguration(new InputStreamReader(resourceStream)) : null;

		random = new Random();

	}

	public String main(@NotNull String key, Object... args) {

		String msg;

		if(!main.containsKey(key)) {

			log.warning("Could not find main message with key " + key);
			msg = key;

		} else
			msg = main.getString(key)
					.formatted(args);

		return msg;
	}

	public String game(@NotNull String key, Object... args) {

		String msg = gameUser.getString(key, "");

		if(msg.isEmpty())
			msg = gameOriginal.getString(key, "");

		if(msg.isEmpty()) {

			log.warning("Could not find game message with key " + key);
			msg = key;

		} else
			msg = msg.formatted(args);

		return msg;
	}

	public String gameRandomPick(@NotNull String key, Object... args) {

		List<String> strings = gameUser.getStringList(key);
		String msg;

		if(strings.isEmpty())
			strings = gameOriginal.getStringList(key);

		if(strings.isEmpty()) {

			log.warning("Could not find game messages with key " + key);
			msg = key;

		} else {
			int i = random.nextInt(strings.size());
			msg = strings.get(i)
					.formatted(args);
		}

		return msg;
	}

}
