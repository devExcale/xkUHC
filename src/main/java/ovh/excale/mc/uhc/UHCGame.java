package ovh.excale.mc.uhc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.uhc.exceptions.GameException;
import ovh.excale.mc.uhc.exceptions.GamePrepareException;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class UHCGame implements TeamedGame {

	private final Set<Challenger> players;
	private final Map<String, Team> teams;
	private final Scoreboard scoreboard;

	private State state;

	public UHCGame() {
		players = Collections.synchronizedSet(new HashSet<>());
		teams = Collections.synchronizedMap(new HashMap<>());

		state = State.PREPARE;

		//noinspection ConstantConditions
		scoreboard = Bukkit.getScoreboardManager()
				.getNewScoreboard();
		// TODO: SCOREBOARD DISPLAYSLOT LIST
	}

	@Override
	public Set<Team> getTeams() {
		return new HashSet<>(teams.values());
	}

	@Override
	public @Nullable Team getTeam(String name) {
		return teams.get(name);
	}

	@Override
	public Team createTeam(@NotNull String name) {
		Team team = new Team(name, scoreboard, false);
		teams.put(team.getName(), team);
		return team;
	}

	@Override
	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	@Override
	public Set<Player> getPlayers() {
		return players.stream()
				.map(Challenger::vanilla)
				.collect(Collectors.toSet());
	}

	@Override
	public Set<Player> getPlayersAlive() {
		return players.stream()
				.filter(Challenger::isAlive)
				.map(Challenger::vanilla)
				.collect(Collectors.toSet());
	}

	@Override
	public Set<Player> getPlayersDead() {
		return players.stream()
				.filter(Challenger::isDead)
				.map(Challenger::vanilla)
				.collect(Collectors.toSet());
	}

	@Override
	public void prepare() throws GamePrepareException {

		// TODO: ASYNC WORLD GENERATION

		state = State.READY;
	}

	@Override
	public void start() throws GameException {

	}

	@Override
	public void reset() throws GameException {

	}

	@Override
	public void stop() {

	}

	@Override
	public State state() {
		return state;
	}

	@Override
	public void addDisconnectListener(BiConsumer<Game, Player> onDisconnect) {

	}

	@Override
	public void addReconnectListener(BiConsumer<Game, Player> onReconnect) {

	}

}
