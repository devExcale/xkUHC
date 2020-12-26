package ovh.excale.mc.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

// TODO: ON LEAVE !IN-GAME, UNWRAP PLAYER & RESET SCOREBOARD
public class Challenger {

	private final UUID uuid;
	private Player player;
	private Team team;
	private Scoreboard scoreboard;

	private boolean alive;
	private boolean online;

	protected Challenger(Player player) {

		uuid = player.getUniqueId();
		this.player = player;
		alive = true;
		team = null;

		ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
		scoreboard = (scoreboardManager != null) ? scoreboardManager.getMainScoreboard() : null;

	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public void setScoreboard(Scoreboard scoreboard) {
		this.scoreboard = scoreboard;
	}

	public void setTeam(@Nullable Team team) {
		this.team = team;
	}

	public @NotNull Player vanilla() {
		return player;
	}

	public @NotNull UUID getUniqueId() {
		return uuid;
	}

	public @Nullable Team getTeam() {
		return team;
	}

	public boolean hasTeam() {
		return team != null;
	}

	public boolean isAlive() {
		return alive;
	}

	public boolean isDead() {
		return !alive;
	}

	public boolean isOnline() {
		return online;
	}

	public void updateReference(Player player) {
		if(uuid.equals(player.getUniqueId()) && player.isOnline()) {
			this.player = player;
			player.setScoreboard(scoreboard);
		}
	}

	public boolean is(Player player) {
		return player.getUniqueId()
				.equals(uuid);
	}

}
