package ovh.excale.xkuhc.comms;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({ "UnusedReturnValue", "unused" })
public class MenuBuilder {

	private static final BaseComponent INDENT;
	private static final BaseComponent SEPARATOR;

	static {
		INDENT = new TextComponent(" - ");

		SEPARATOR = new TextComponent("================================");
		SEPARATOR.setBold(true);
		SEPARATOR.setUnderlined(false);
		SEPARATOR.setHoverEvent(null);
		SEPARATOR.setClickEvent(null);
		SEPARATOR.setColor(ChatColor.GRAY);
	}

	private final ComponentBuilder builder;
	private final List<BaseComponent> elements;
	private final BaseComponent title;
	private TextComponent last;
	private String info;

	public MenuBuilder(String title) {
		builder = new ComponentBuilder("\n\n\n\n").append(SEPARATOR, FormatRetention.NONE);
		elements = new LinkedList<>();
		this.title = new TextComponent(" { " + title + " }");

		this.title.setBold(true);
		this.title.setColor(ChatColor.DARK_GREEN);
	}

	public MenuBuilder insert(String value, HoverEvent onHover, ClickEvent onClick) {
		TextComponent textComponent = new TextComponent(value);
		textComponent.setUnderlined(onClick != null || onHover != null);
		textComponent.setHoverEvent(onHover);
		textComponent.setClickEvent(onClick);

		elements.add(textComponent);
		return this;
	}

	public MenuBuilder info(String string) {
		info = string;
		return this;
	}

	public MenuBuilder last(TextComponent textComponent) {
		last = textComponent;
		return this;
	}

	public BaseComponent[] build() {

		int size = elements.size() + 6;
		size += last == null ? 0 : 2;
		size += info == null ? 0 : 1;

		// TITLE
		builder.append("\n\n", FormatRetention.NONE)
				.append(title, FormatRetention.NONE)
				.append("\n", FormatRetention.NONE);

		// INFO
		if(info != null)
			builder.append(new TextComponent("\n" + info), FormatRetention.NONE);

		// ALL OPTIONS
		for(BaseComponent element : elements)
			builder.append("\n", FormatRetention.NONE)
					.append(INDENT, FormatRetention.NONE)
					.append(element, FormatRetention.NONE);

		// BLANK LINES
		for(int i = size; i < 20; i++)
			builder.append("\n", FormatRetention.NONE);

		// LAST OPTION
		if(last != null)
			builder.append(new TextComponent("\n\n> "), FormatRetention.NONE)
					.append(last, FormatRetention.NONE);

		// BUILD
		return builder.append("\n\n", FormatRetention.NONE)
				.append(SEPARATOR, FormatRetention.NONE)
				.create();
	}

}
