package ovh.excale.mc.uhc.core;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import ovh.excale.mc.uhc.Game;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Basically a team.
 */
public class Bond {

	private final Map<UUID, Gamer> gamers;
	private final Game game;
	private final String name;

	private ChatColor color;
	private boolean friendlyFire;

	protected Bond(String name, Game game) {
		gamers = Collections.synchronizedMap(new HashMap<>());
		this.game = game;
		this.name = name;

		color = ChatColor.WHITE;
		friendlyFire = false;
	}

	protected void setColor(ChatColor color) {
		this.color = color != null ? color : ChatColor.WHITE;
	}

	protected void setFriendlyFire(boolean friendlyFire) {
		this.friendlyFire = friendlyFire;
	}

	public ChatColor getColor() {
		return color;
	}

	public String getName() {
		return name;
	}

	public int size() {
		return gamers.size();
	}

	public Game getGame() {
		return game;
	}

	public Stream<Gamer> getGamers() {
		return gamers.values()
				.stream();
	}

	protected Map<UUID, Gamer> getInternalGamersMap() {
		return gamers;
	}

	public boolean isFriendlyFire() {
		return friendlyFire;
	}

	public void broadcast(@NotNull String message) {
		gamers.forEach((uuid, gamer) -> gamer.getPlayer()
				.sendMessage(message));
	}

	public void broadcast(@NotNull BaseComponent message) {
		gamers.forEach((uuid, gamer) -> gamer.getPlayer()
				.spigot()
				.sendMessage(message));
	}

}
