package ovh.excale.mc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.api.Game;
import ovh.excale.mc.api.Team;
import ovh.excale.mc.api.TeamedGame;

import java.util.*;
import java.util.function.BiConsumer;

public class UhcGame implements TeamedGame {

	private final Map<UUID, Challenger> players;
	private final Scoreboard scoreboard;
	private final TeamManager teamManager;
	private final ChallengerManager challengerManager;

	private UUID adminId;

	private State state;

	public UhcGame(@NotNull Player admin) {
		//noinspection ConstantConditions
		scoreboard = Bukkit.getScoreboardManager()
				.getNewScoreboard();

		players = Collections.synchronizedMap(new HashMap<>());
		teamManager = new TeamManager(this);
		challengerManager = new ChallengerManager();
		adminId = admin.getUniqueId();

		state = State.PREPARE;

		// TODO: SCOREBOARD DISPLAYSLOT LIST
	}

	@Override
	public @NotNull Set<Team> getTeams() {
		return teamManager.getTeams();
	}

	@Override
	public @Nullable Team getTeam(String name) {
		return teamManager.getTeam(name);
	}

	@Override
	public Team createTeam(@NotNull String name) throws IllegalStateException {

		if(!state.isEditable())
			throw new IllegalStateException("Game is past preparation phase");

		return teamManager.registerNewTeam(name);
	}

	@Override
	public void unregisterTeam(String name) throws IllegalStateException {

		if(!state.isEditable())
			throw new IllegalStateException("Game is past preparation phase");

		// TODO: REMOVE CHALLENGERS FROM CHAL_MANAGER ON SET_TEAM NULL
		teamManager.unregisterTeam(name);
	}

	@Override
	public @NotNull Scoreboard getScoreboard() {
		return scoreboard;
	}

	public @NotNull ChallengerManager getChallengerManager() {
		return challengerManager;
	}

	@Override
	public Set<Player> getPlayers() {
		Set<Player> set = new HashSet<>();

		for(Challenger challenger : players.values())
			set.add(challenger.vanilla());

		return set;
	}

	@Override
	public Set<Player> getSpectators() {
		return null;
	}

	@Override
	public void prepare() throws IllegalStateException {

		// TODO: ASYNC WORLD GENERATION

		state = State.READY;
	}

	@Override
	public void start() throws IllegalStateException {

	}

	@Override
	public void reset() throws IllegalStateException {

	}

	@Override
	public void stop() {

	}

	@Override
	public @NotNull State getState() {
		return state;
	}

	@Override
	public @NotNull UUID getAdminId() {
		return adminId;
	}

	@Override
	public boolean isReady() {

		// TODO: CHECK TEAMS & WORLD STATE

		return false;
	}

	@Override
	public void setAdmin(Player player) {
		adminId = player.getUniqueId();
	}

	@Override
	public void setDisconnectListener(BiConsumer<Game, Player> onDisconnect) {

	}

	@Override
	public void setReconnectListener(BiConsumer<Game, Player> onReconnect) {

	}

	@Override
	public void resetListeners() {

	}

}
