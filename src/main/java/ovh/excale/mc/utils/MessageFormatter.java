package ovh.excale.mc.utils;

import org.bukkit.ChatColor;
import ovh.excale.mc.uhc.core.Bond;
import ovh.excale.mc.uhc.core.Gamer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MessageFormatter {

	public static MessageFormatter create() {
		MessageFormatter formatter = new MessageFormatter();

		for(ChatColor value : ChatColor.values())
			formatter.map.put(value.name(), value.toString());

		return formatter;
	}

	private final Map<String, String> map;

	private MessageFormatter() {
		map = new HashMap<>();
	}

	public MessageFormatter with(Bond bond) {

		map.put("bond", bond.getName());
		map.put("bondColor", bond.getColor()
				.toString());

		return this;
	}

	public MessageFormatter with(Gamer gamer) {

		map.put("gamer", gamer.getPlayer()
				.getDisplayName());

		return this;
	}

	public String format(final String message) {

		String s = message;

		for(Entry<String, String> entry : map.entrySet())
			s = s.replaceAll("\\{" + entry.getKey() + "}", entry.getValue());

		return s;
	}

}
