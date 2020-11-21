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
	private final Scoreboard scoreboard;
	private final TeamManager teamManager;
	private final Map<UUID, Challenger> disconnectPool;

	private State state;

	public UHCGame() {
		//noinspection ConstantConditions
		scoreboard = Bukkit.getScoreboardManager()
				.getNewScoreboard();

		players = Collections.synchronizedMap(new HashMap<>());
		disconnectPool = Collections.synchronizedMap(new HashMap<>());
		teamManager = new TeamManager(scoreboard);

		state = State.PREPARE;

		// TODO: SCOREBOARD DISPLAYSLOT LIST
	}

	@Override
	public Set<Team> getTeams() {
		return teamManager.getTeams();
	}

	@Override
	public @Nullable Team getTeam(String name) {
		return teamManager.getTeam(name);
	}

	@Override
	public Team createTeam(@NotNull String name) {
		return teamManager.registerNewTeam(name);
	}

	@Override
	public void unregisterTeam(String name) {
		teamManager.unregisterTeam(name);
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
