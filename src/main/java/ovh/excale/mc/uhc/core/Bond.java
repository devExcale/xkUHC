package ovh.excale.mc.uhc.core;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import ovh.excale.mc.uhc.Game;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Basically a team.
 */
public class Bond {

	private final Set<Gamer> gamers;
	private final Game game;
	private final String name;

	private ChatColor color;
	private boolean friendlyFire;

	protected Bond(String name, Game game) {
		gamers = Collections.synchronizedSet(new HashSet<>());
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

	public Set<Gamer> getGamers() {
		return new HashSet<>(gamers);
	}

	protected Set<Gamer> getInternalGamersSet() {
		return gamers;
	}

	public boolean isFriendlyFireEnabled() {
		return friendlyFire;
	}

	public void broadcast(@NotNull String message) {
		for(Gamer gamer : gamers)
			gamer.getPlayer()
					.sendMessage(message);
	}

	public void broadcast(@NotNull BaseComponent message) {
		for(Gamer gamer : gamers)
			gamer.getPlayer()
					.spigot()
					.sendMessage(message);
	}

}
