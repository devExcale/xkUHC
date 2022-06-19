package ovh.excale.xkuhc.comms;

import org.bukkit.ChatColor;
import ovh.excale.xkuhc.core.Bond;
import ovh.excale.xkuhc.core.Gamer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.*;

public class MessageFormatter {

	private final Map<String, String> map;
	private final String message;

	public MessageFormatter(String message) {
		this.message = message;
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

	public MessageFormatter killer(Gamer gamer) {

		map.put("killer", gamer.getPlayer()
				.getName());

		Bond bond = gamer.getBond();

		map.put("killerBond", bond.getName());
		map.put("killerBondColor", bond.getColor()
				.toString());

		return this;
	}

	public MessageFormatter custom(String key, Object value) {

		map.put(key, value.toString());

		return this;
	}

	public String format() {
		return format(message);
	}

	private String format(final String message) {

		String s = message;

		for(Entry<String, String> entry : map.entrySet())
			s = s.replaceAll("\\{" + entry.getKey() + "}", entry.getValue());

		return s;
	}

	public String formatSpecial(ChatColor... formats) {

		String joinedFormats = Arrays.stream(formats)
				.map(ChatColor::toString)
				.collect(Collectors.joining());

		return joinedFormats + format(message.replaceAll("\\{RESET}", RESET + joinedFormats));
	}

	public String formatFine() {
		return formatSpecial(GRAY, ITALIC);
	}

	public String formatFail() {
		return formatSpecial(RED);
	}

	public String formatAccent() {
		return formatSpecial(GOLD);
	}

}
