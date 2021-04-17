package ovh.excale.mc.uhc.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import ovh.excale.mc.uhc.Game;
import ovh.excale.mc.uhc.misc.ScoreboardPrinter;

import java.util.UUID;

public class Gamer {

	private final Scoreboard scoreboard;
	private final ScoreboardPrinter scoreboardPrinter;
	private final Game game;

	private Player player;
	private Bond bond;
	private int killCount;
	private boolean alive;

	protected Gamer(Game game, Player player) {
		this.player = player;
		this.game = game;
		bond = null;
		alive = true;
		killCount = 0;

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

	public void resetKillCount() {
		killCount = 0;
	}

	public void incrementKillCount() {
		killCount++;
	}

	public Game getGame() {
		return game;
	}

	public Bond getBond() {
		return bond;
	}

	public int getKillCount() {
		return killCount;
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

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
