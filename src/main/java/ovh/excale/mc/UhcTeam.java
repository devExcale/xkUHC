package ovh.excale.mc;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ovh.excale.mc.api.Team;
import ovh.excale.mc.api.TeamedGame;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

public class UhcTeam implements Team {

	private final String name;
	private final Set<Challenger> players;
	private final org.bukkit.scoreboard.Team vanillaTeam;
	private ChatColor color;
	private boolean eliminated;
	private UhcGame game;

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

	@Override
	public boolean remove(Player player) {
		Challenger challenger = game.getChallengerManager()
				.wrap(player);

		boolean b = this.equals(challenger.getTeam());

		if(b)
			try {
				vanillaTeam.removeEntry(player.getName());
				players.remove(challenger);
				challenger.setTeam(null);
			} catch(IllegalStateException e) {
				UHC.logger()
						.log(Level.FINER, "Calling Team::removeEntry on an unregistered vanilla team", e);
			}

		return b;
	}

	@Override
	public boolean add(Player player) {
		Challenger challenger = game.getChallengerManager()
				.wrap(player);

		boolean b = challenger.getTeam() == null;

		if(b)
			try {
				vanillaTeam.addEntry(player.getName());
				players.add(challenger);
				challenger.setAlive(true);
				challenger.setTeam(this);
			} catch(IllegalStateException e) {
				UHC.logger()
						.log(Level.FINER, "Calling Team::addEntry on an unregistered vanilla team", e);
			}

		return b;
	}

	@Override
	public void unregister() {

		for(Challenger challenger : players) {
			challenger.setTeam(null);
			challenger.vanilla()
					.sendMessage("Your party has been disbanded.");
		}
		players.clear();

		try {
			vanillaTeam.unregister();
		} catch(IllegalStateException e) {
			UHC.logger()
					.log(Level.FINER, "Calling Team::unregister on an unregistered vanilla team", e);
		}
	}

	@Override
	public void setColor(ChatColor color) {
		try {

			this.color = (color != null) ? color : ChatColor.WHITE;
			vanillaTeam.setColor(this.color);

		} catch(IllegalStateException e) {
			UHC.logger()
					.log(Level.FINER, "Calling Team::setColor on an unregistered vanilla team", e);
		}
	}

	@Override
	public void setFriendlyFire(boolean friendlyFire) {
		try {

			vanillaTeam.setAllowFriendlyFire(friendlyFire);

		} catch(IllegalStateException e) {
			UHC.logger()
					.log(Level.FINER, "Calling Team::setAllowFriendlyFire on an unregistered vanilla team", e);
		}
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
