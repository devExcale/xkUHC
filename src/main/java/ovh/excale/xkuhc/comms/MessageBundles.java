package ovh.excale.xkuhc.comms;

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
	private final ResourceBundle discord;
	private final YamlConfiguration gameOriginal;
	private final YamlConfiguration gameUser;

	private final Logger log;
	private final Random random;

	public MessageBundles(@NotNull Plugin plugin) {

		log = plugin.getLogger();

		main = ResourceBundle.getBundle("messages.main");
		discord = ResourceBundle.getBundle("messages.discord");

		//noinspection ConstantConditions
		gameOriginal = YamlConfiguration.loadConfiguration(new InputStreamReader(getClass().getClassLoader()
				.getResourceAsStream("messages/game.yml")));

		InputStream resourceStream = plugin.getResource("messages/game.yml");
		gameUser = (resourceStream != null) ? YamlConfiguration.loadConfiguration(new InputStreamReader(resourceStream)) : null;

		random = new Random();

	}

	public MessageFormatter main(@NotNull String key) {

		String msg;

		if(!main.containsKey(key)) {

			log.warning("Could not find main message with key " + key);
			msg = key;

		} else
			msg = main.getString(key);

		return new MessageFormatter(msg).addColors();
	}

	public String mainRaw(@NotNull String key, Object... args) {

		String msg;

		if(!main.containsKey(key)) {

			log.warning("Could not find main message with key " + key);
			msg = key;

		} else
			msg = main.getString(key)
					.formatted(args);

		return msg;
	}

	public MessageFormatter discord(@NotNull String key) {

		String msg;

		if(!discord.containsKey(key)) {

			log.warning("Could not find discord message with key " + key);
			msg = key;

		} else
			msg = discord.getString(key);

		return new MessageFormatter(msg).addColors();
	}

	public String discordRaw(@NotNull String key, Object... args) {

		String msg;

		if(!discord.containsKey(key)) {

			log.warning("Could not find discord message with key " + key);
			msg = key;

		} else
			msg = discord.getString(key)
					.formatted(args);

		return msg;
	}

	public MessageFormatter game(@NotNull String key) {

		String msg = gameUser.getString(key, "");

		if(msg.isEmpty())
			msg = gameOriginal.getString(key, "");

		if(msg.isEmpty()) {

			log.warning("Could not find game message with key " + key);
			msg = key;

		}

		return new MessageFormatter(msg).addColors();
	}

	public String gameRaw(@NotNull String key, Object... args) {

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

	public MessageFormatter gameRandomPick(@NotNull String key) {

		List<String> strings = gameUser.getStringList(key);
		String msg;

		if(strings.isEmpty())
			strings = gameOriginal.getStringList(key);

		if(strings.isEmpty()) {

			log.warning("Could not find game messages with key " + key);
			msg = key;

		} else {

			int i = random.nextInt(strings.size());
			msg = strings.get(i);

		}

		return new MessageFormatter(msg).addColors();
	}

}
