package ovh.excale.mc;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.api.Team;

import java.util.UUID;

// TODO: ON LEAVE !IN-GAME, UNWRAP PLAYER & RESET SCOREBOARD
public class Challenger {

	private final UUID uuid;
	private Player player;
	private Team team;
	private boolean alive;

	protected Challenger(Player player) {
		uuid = player.getUniqueId();
		this.player = player;
		alive = true;
		team = null;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
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

	public boolean is(Player player) {
		return player.getUniqueId()
				.equals(uuid);
	}

}
