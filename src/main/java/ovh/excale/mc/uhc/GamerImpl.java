package ovh.excale.mc.uhc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.core.Gamer;

import java.util.UUID;

public class GamerImpl implements Gamer {

	private Player player;
	private BondImpl bond;
	private boolean alive;

	private Scoreboard scoreboard;

	protected GamerImpl(Player player) {
		this.player = player;
		alive = true;

		//noinspection ConstantConditions
		scoreboard = Bukkit.getScoreboardManager()
				.getMainScoreboard();

	}

	@Override
	public @Nullable BondImpl getBond() {
		return bond;
	}

	@Override
	public @NotNull UUID getUniqueId() {
		return player.getUniqueId();
	}

	@Override
	public @NotNull Player getPlayer() {
		return player;
	}

	@Override
	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	@Override
	public boolean isAlive() {
		return alive;
	}

	@Override
	public boolean isOnline() {
		return player.isOnline();
	}

	@Override
	public boolean hasBond() {
		return bond != null;
	}

	protected void setBond(BondImpl bond) {
		this.bond = bond;

		if(bond == null)
			//noinspection ConstantConditions
			scoreboard = Bukkit.getScoreboardManager()
					.getMainScoreboard();
		else
			scoreboard = bond.getGame()
					.getScoreboard();

		player.setScoreboard(scoreboard);
	}

	protected void updateReference(Player player) {
		this.player = player;
		player.setScoreboard(scoreboard);
	}

}
