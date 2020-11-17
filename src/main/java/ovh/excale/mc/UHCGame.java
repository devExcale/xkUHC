package ovh.excale.mc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.exceptions.GameException;
import ovh.excale.mc.exceptions.GamePrepareException;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class UHCGame implements TeamedGame {

	private final Map<UUID, Challenger> players;
	private final Map<String, Team> teams;
	private final Scoreboard scoreboard;
	private final Map<UUID, Challenger> disconnectPool;

	private State state;

	public UHCGame() {
		players = Collections.synchronizedMap(new HashMap<>());
		teams = Collections.synchronizedMap(new HashMap<>());
		disconnectPool = Collections.synchronizedMap(new HashMap<>());

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
	public Set<Player> unregisterTeam(String name) {
		Set<Player> members = new HashSet<>();

		if(teams.containsKey(name))
			members.addAll(teams.remove(name)
					.members());

		return members;
	}

	@Override
	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	@Override
	public Set<Player> getPlayers() {
		return players.values()
				.stream()
				.map(Challenger::vanilla)
				.collect(Collectors.toSet());
	}

	@Override
	public Set<Player> getPlayersAlive() {
		return players.values()
				.stream()
				.filter(Challenger::isAlive)
				.map(Challenger::vanilla)
				.collect(Collectors.toSet());
	}

	@Override
	public Set<Player> getPlayersDead() {
		return players.values()
				.stream()
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
