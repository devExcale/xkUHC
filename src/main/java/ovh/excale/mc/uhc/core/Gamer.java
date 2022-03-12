package ovh.excale.mc.uhc.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import ovh.excale.mc.uhc.Game;
import ovh.excale.mc.uhc.misc.ScoreboardPrinter;

import java.util.Collection;
import java.util.UUID;

public class Gamer {

	private final Scoreboard scoreboard;
	private final ScoreboardPrinter scoreboardPrinter;
	private final Game game;

	private Player player;
	private Bond bond;
	private int killCount;
	private boolean alive;

	private Double healthSnapshot;
	private Integer xpSnapshot;
	private Location locationSnapshot;
	private ItemStack[] inventorySnapshot;
	private Collection<PotionEffect> activeEffectsSnapshot;

	protected Gamer(Game game, Player player) {
		this.player = player;
		this.game = game;
		bond = null;
		alive = true;
		killCount = 0;

		healthSnapshot = null;
		locationSnapshot = null;
		inventorySnapshot = null;
		activeEffectsSnapshot = null;

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

	/**
	 * Resets the underlying player to normal health, inventory
	 */
	protected void resetPlayer() {

		//noinspection ConstantConditions
		player.setScoreboard(Bukkit.getScoreboardManager()
				.getMainScoreboard());

		//noinspection ConstantConditions
		player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
				.setBaseValue(20);
		player.setHealth(20);

		player.setTotalExperience(0);

		player.teleport(Bukkit.getWorlds()
				.get(0)
				.getSpawnLocation());

		for(PotionEffect effect : player.getActivePotionEffects())
			player.removePotionEffect(effect.getType());

	}

	// TODO: UHC-MODE (apply effects/scale health)

	// TODO: SNAPSHOT CLASS
	protected void takeSnapshot() {

		healthSnapshot = player.getHealth();
		xpSnapshot = player.getTotalExperience();
		locationSnapshot = player.getLocation();
		activeEffectsSnapshot = player.getActivePotionEffects();
		inventorySnapshot = player.getInventory()
				.getContents();

	}

	protected void applySnapshot(Player player) {

		if(player != null)
			this.player = player;

		// set scoreboard
		this.player.setScoreboard(scoreboard);

		//set health
		//noinspection ConstantConditions
		this.player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
				.setBaseValue(40d);
		this.player.setHealth(healthSnapshot);

		// set experience
		this.player.setTotalExperience(xpSnapshot);

		// teleport player back to where he was
		this.player.teleport(locationSnapshot);

		// replace inventory
		this.player.getInventory()
				.setContents(inventorySnapshot);

		for(PotionEffect effect : activeEffectsSnapshot)
			this.player.addPotionEffect(effect, true);

	}

}
