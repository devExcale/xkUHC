package ovh.excale.mc.uhc;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import ovh.excale.mc.core.Bond;
import ovh.excale.mc.core.Gamer;

import java.util.*;

public class BondImpl implements Bond {

	private final Map<UUID, GamerImpl> gamers;
	private final GameImpl game;
	private final String name;
	private final Team team;

	private ChatColor color;
	private boolean frozen;

	protected BondImpl(String name, GameImpl game) {
		gamers = Collections.synchronizedMap(new HashMap<>());
		frozen = false;
		this.game = game;
		this.name = name;

		color = ChatColor.WHITE;
		team = game.getScoreboard()
				.registerNewTeam(name);
		team.setColor(color);

	}

	private void frozenCheck() throws IllegalStateException {

		if(frozen)
			throw new IllegalStateException("This bond won't take any more changes");

	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void boundGamer(@NotNull Gamer gamer) throws IllegalStateException, IllegalArgumentException {

		frozenCheck();

		GamerImpl gamer1 = (GamerImpl) Objects.requireNonNull(gamer);
		if(gamer1.hasBond())
			throw new IllegalStateException("This gamer has already bonded");

		gamer1.setBond(this);
		gamers.put(gamer1.getUniqueId(), gamer1);
		team.addEntry(gamer1.getPlayer()
				.getName());

	}

	@Override
	public void unboundGamer(@NotNull Gamer gamer) throws IllegalStateException, IllegalArgumentException {

		frozenCheck();

		GamerImpl gamer1 = (GamerImpl) Objects.requireNonNull(gamer);
		if(!gamer.hasBond())
			throw new IllegalStateException("This game does not have a bond");

		gamer1.setBond(null);
		gamers.remove(gamer1.getUniqueId());
		team.removeEntry(gamer1.getPlayer()
				.getName());

	}

	protected void breakBond() throws IllegalStateException {

		frozenCheck();

		gamers.forEach((uuid, gamer) -> {

			gamer.setBond(null);
			team.removeEntry(gamer.getPlayer()
					.getName());

		});

		gamers.clear();

	}

	@Override
	public Set<GamerImpl> getGamers() {
		return new HashSet<>(gamers.values());
	}

	@Override
	public GameImpl getGame() {
		return game;
	}

	@Override
	public void freeze() throws IllegalStateException {

		frozenCheck();
		frozen = true;

	}

	@Override
	public boolean isFrozen() {
		return frozen;
	}

	@Override
	public void broadcast(String message) {

		if(message != null)
			gamers.forEach((uuid, gamer) -> gamer.getPlayer()
					.sendMessage(message));

	}

	@Override
	public void setColor(ChatColor color) throws IllegalStateException {

		frozenCheck();
		this.color = color != null ? color : ChatColor.WHITE;
		team.setColor(this.color);

	}

	@Override
	public ChatColor getColor() {
		return color;
	}

	@Override
	public void setFriendlyFire(boolean friendlyFire) {

		frozenCheck();
		team.setAllowFriendlyFire(friendlyFire);

	}

	@Override
	public boolean isFriendlyFire() {
		return team.allowFriendlyFire();
	}

}
