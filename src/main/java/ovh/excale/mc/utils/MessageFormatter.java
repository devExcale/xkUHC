package ovh.excale.mc.utils;

import org.bukkit.ChatColor;
import ovh.excale.mc.uhc.core.Bond;
import ovh.excale.mc.uhc.core.Gamer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.bukkit.ChatColor.*;

public class MessageFormatter {

	public static MessageFormatter with(Gamer gamer, Bond bond) {

		MessageFormatter formatter = new MessageFormatter().addColors();

		if(gamer != null)
			formatter.gamer(gamer);

		if(bond != null)
			formatter.bond(bond);

		return formatter;
	}

	public static MessageFormatter with(Bond bond) {
		return with(null, bond);
	}

	public static MessageFormatter with(Gamer gamer) {
		return with(gamer, null);
	}

	private final Map<String, String> map;

	public MessageFormatter() {
		map = new HashMap<>();
	}

	public MessageFormatter addColors() {

		for(ChatColor value : ChatColor.values())
			map.put(value.name(), value.toString());

		return this;
	}

	public MessageFormatter bond(Bond bond) {

		map.put("bond", bond.getName());
		map.put("bondColor", bond.getColor()
				.toString());

		return this;
	}

	public MessageFormatter bond(String bondName) {

		map.put("bond", bondName);

		return this;
	}

	public MessageFormatter gamer(Gamer gamer) {

		map.put("gamer", gamer.getPlayer()
				.getDisplayName());

		return this;
	}

	public MessageFormatter gamer(String gamerName) {

		map.put("gamer", gamerName);

		return this;
	}

	public MessageFormatter custom(String key, Object value) {

		map.put(key, value.toString());

		return this;
	}

	public String format(final String message) {

		String s = message;

		for(Entry<String, String> entry : map.entrySet())
			s = s.replaceAll("\\{" + entry.getKey() + "}", entry.getValue());

		return s;
	}

	public String formatFine(String message) {
		return GRAY.toString() + ITALIC + format(message.replaceAll("\\{RESET}", "{RESET}{GRAY}{ITALIC}"));
	}

	public String formatFail(String message) {
		return RED + format(message.replaceAll("\\{RESET}", "{RESET}{RED}"));
	}

}
