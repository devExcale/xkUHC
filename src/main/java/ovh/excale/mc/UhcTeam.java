package ovh.excale.mc;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ovh.excale.mc.api.Game;
import ovh.excale.mc.api.Team;
import ovh.excale.mc.api.TeamedGame;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class UhcTeam implements Team {

	private final String name;
	private final Set<Challenger> players;
	private final org.bukkit.scoreboard.Team vanillaTeam;
	private final UhcGame game;
	private ChatColor color;
	private boolean eliminated;

	protected UhcTeam(@NotNull String name, @NotNull TeamedGame game) throws IllegalArgumentException {
		players = Collections.synchronizedSet(new HashSet<>());
		this.name = Objects.requireNonNull(name);

		color = ChatColor.WHITE;
		eliminated = false;

		if(!(Objects.requireNonNull(game) instanceof UhcGame))
			throw new IllegalArgumentException("Game must be of Uhc type");

		this.game = (UhcGame) game;

		vanillaTeam = game.getScoreboard()
				.registerNewTeam(name);
		vanillaTeam.setCanSeeFriendlyInvisibles(true);
		vanillaTeam.setAllowFriendlyFire(false);
	}

	private void checkState() throws IllegalStateException {

		if(game.getState() == Game.State.RUNNING)
			throw new IllegalStateException("Cannot edit team while game is running");

	}

	@Override
	public boolean remove(Player player) throws IllegalStateException {

		checkState();
		Challenger challenger = game.getChallengerManager()
				.wrap(player);
		boolean b = this.equals(challenger.getTeam());

		if(b) {
			vanillaTeam.removeEntry(player.getName());
			players.remove(challenger);
			challenger.setTeam(null);
		}

		return b;
	}

	@Override
	public boolean add(Player player) throws IllegalStateException {

		checkState();
		Challenger challenger = game.getChallengerManager()
				.wrap(player);
		boolean b = challenger.getTeam() == null;

		if(b) {
			vanillaTeam.addEntry(player.getName());
			players.add(challenger);
			challenger.setAlive(true);
			challenger.setTeam(this);
		}

		return b;
	}

	@Override
	public void unregister() throws IllegalStateException {

		checkState();
		for(Challenger challenger : players) {
			challenger.setTeam(null);
			challenger.vanilla()
					.sendMessage("Your party has been disbanded.");
		}

		players.clear();
		vanillaTeam.unregister();
	}

	@Override
	public void setColor(ChatColor color) throws IllegalStateException {

		checkState();
		this.color = (color != null) ? color : ChatColor.WHITE;
		vanillaTeam.setColor(this.color);

	}

	@Override
	public void setFriendlyFire(boolean friendlyFire) throws IllegalStateException {

		checkState();
		vanillaTeam.setAllowFriendlyFire(friendlyFire);

	}

	@Override
	public @NotNull TeamedGame getGame() {
		return game;
	}

	@Override
	public @NotNull String getName() {
		return name;
	}

	@Override
	public @NotNull Set<Player> getMembers() {
		Set<Player> set = new HashSet<>();

		for(Challenger challenger : players)
			set.add(challenger.vanilla());

		return set;
	}

	@Override
	public @NotNull ChatColor getColor() {
		return color;
	}

	@Override
	public boolean getFriendlyFire() {
		return vanillaTeam.allowFriendlyFire();
	}

	@Override
	public boolean isEliminated() {
		if(!eliminated)
			eliminated = players.stream()
					.noneMatch(Challenger::isAlive);

		return eliminated;
	}

	@Override
	public boolean isAlive() {
		if(!eliminated)
			eliminated = players.stream()
					.noneMatch(Challenger::isAlive);

		return !eliminated;
	}

}
