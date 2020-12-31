package ovh.excale.mc.api;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class UhcTeam implements Team {

	private final String name;
	private final Set<Challenger> players;
	private final org.bukkit.scoreboard.Team vanillaTeam;
	private final UhcGame game;
	private final ChallengerManager challengerManager;
	private ChatColor color;

	private boolean eliminated;
	private boolean unregistered;

	public UhcTeam(@NotNull String name, TeamedGame game) throws IllegalArgumentException {
		players = Collections.synchronizedSet(new HashSet<>());
		this.name = Objects.requireNonNull(name);

		color = ChatColor.WHITE;
		eliminated = false;

		this.game = (UhcGame) Objects.requireNonNull(game);
		challengerManager = this.game.getChallengerManager();

		vanillaTeam = game.getScoreboard()
				.registerNewTeam(name);
		vanillaTeam.setCanSeeFriendlyInvisibles(true);
		vanillaTeam.setAllowFriendlyFire(false);
	}

	@Override
	public boolean remove(Player player) throws IllegalStateException {

		game.editableCheck();
		Challenger challenger = challengerManager.get(player.getUniqueId());
		boolean b = challenger != null && this.equals(challenger.getTeam());

		if(b) {
			vanillaTeam.removeEntry(player.getName());
			players.remove(challenger);
			challenger.setScoreboard(null);
			challenger.setTeam(null);
		}

		return b;
	}

	@Override
	public boolean add(Player player) throws IllegalStateException {

		game.editableCheck();
		Challenger challenger = challengerManager.get(player.getUniqueId());
		if(challenger == null)
			challenger = challengerManager.register(player);

		Team team = challenger.getTeam();
		boolean b = team == null;
		if(b) {
			vanillaTeam.addEntry(player.getName());
			players.add(challenger);
			player.sendMessage("You've been added to team " + getName() + ".");
			challenger.setScoreboard(game.getScoreboard());
			challenger.setAlive(true);
			challenger.setTeam(this);
		}

		return b;
	}

	// TODO: NEED TO EDIT ACCESS MODIFIERS SOMETIME
	@Override
	public void unregister() throws IllegalStateException {
		unregister(true);
	}

	@Override
	public void unregister(boolean silent) throws IllegalStateException {

		game.editableCheck();
		for(Challenger challenger : players) {
			challenger.setScoreboard(null);
			challenger.setTeam(null);
			if(!silent)
				challenger.vanilla()
						.sendMessage("Your party has been disbanded.");
		}

		game.getTeamManager()
				.removeTeam(this);
		players.clear();
		vanillaTeam.unregister();
	}

	@Override
	public void setColor(ChatColor color) throws IllegalStateException {

		game.editableCheck();
		this.color = (color != null) ? color : ChatColor.WHITE;
		vanillaTeam.setColor(this.color);

	}

	@Override
	public void setFriendlyFire(boolean friendlyFire) throws IllegalStateException {

		game.editableCheck();
		vanillaTeam.setAllowFriendlyFire(friendlyFire);

	}

	@Override
	public void validate() {
		for(Challenger challenger : players)
			if(!challenger.isOnline())
				remove(challenger.vanilla());
	}

	@Override
	public void broadcast(String message) {
		if(!unregistered)
			players.forEach(challenger -> challenger.vanilla()
					.sendMessage(message));
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
	public @NotNull Set<Challenger> getChallengers() {
		return new HashSet<>(players);
	}

	@Override
	public @NotNull Player[] getMembersAsArray() {
		return players.stream()
				.map(Challenger::vanilla)
				.toArray(Player[]::new);
	}

	@Override
	public @NotNull Challenger[] getChallengersAsArray() {
		return players.toArray(new Challenger[0]);
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
