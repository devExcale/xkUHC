package ovh.excale.mc.uhc.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.uhc.misc.ScoreboardPrinter;

import java.util.UUID;

public class Gamer {

	private final Scoreboard scoreboard;
	private final ScoreboardPrinter scoreboardPrinter;

	private Player player;
	private Bond bond;
	private boolean alive;

	protected Gamer(Player player) {
		this.player = player;
		bond = null;
		alive = true;

		//noinspection ConstantConditions
		scoreboard = Bukkit.getScoreboardManager()
				.getNewScoreboard();
		scoreboard.registerNewObjective("health", "health", "health", RenderType.HEARTS)
				.setDisplaySlot(DisplaySlot.PLAYER_LIST);

		player.setScoreboard(scoreboard);
		scoreboardPrinter = new ScoreboardPrinter(this);

	}

	protected void setBond(Bond bond) {
		this.bond = bond;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public @Nullable Bond getBond() {
		return bond;
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public ScoreboardPrinter getScoreboardPrinter() {
		return scoreboardPrinter;
	}

	public @NotNull UUID getUniqueId() {
		return player.getUniqueId();
	}

	public @NotNull Player getPlayer() {
		return player;
	}

	public boolean isAlive() {
		return alive;
	}

	public boolean isOnline() {
		return player.isOnline();
	}

	public boolean hasBond() {
		return bond != null;
	}

	protected void updateReference(Player player) {
		this.player = player;
		player.setScoreboard(scoreboard);
	}

}
