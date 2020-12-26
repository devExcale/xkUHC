package ovh.excale.mc;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.api.Game;
import ovh.excale.mc.api.Team;
import ovh.excale.mc.api.TeamManager;
import ovh.excale.mc.api.TeamedGame;
import ovh.excale.mc.utils.PlayerSpreader;
import ovh.excale.mc.utils.RandomUhcWorldGenerator;

import java.util.*;
import java.util.logging.Level;

// TODO: RIGHT SCOREBOARD - TEAM & GAME INFO
public class UhcGame implements TeamedGame {

	private final Map<UUID, Challenger> players;
	private final Scoreboard scoreboard;
	private final TeamManager teamManager;
	private final ChallengerManager challengerManager;
	private final RandomUhcWorldGenerator worldGenerator;

	private UUID adminId;
	private Status status;
	private World world;

	public UhcGame(@NotNull Player admin) {
		//noinspection ConstantConditions
		scoreboard = Bukkit.getScoreboardManager()
				.getNewScoreboard();

		players = Collections.synchronizedMap(new HashMap<>());
		adminId = admin.getUniqueId();
		teamManager = new TeamManager(this);
		challengerManager = new ChallengerManager(this);
		worldGenerator = new RandomUhcWorldGenerator(UHC.plugin(), System.currentTimeMillis());

		status = Status.PREPARE;

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

		if(!status.isEditable())
			throw new IllegalStateException("Game is past preparation phase");

		return teamManager.registerNewTeam(name);
	}

	@Override
	public boolean unregisterTeam(String name) throws IllegalStateException {

		if(!status.isEditable())
			throw new IllegalStateException("Game is past preparation phase");

		// TODO: REMOVE CHALLENGERS FROM CHAL_MANAGER ON SET_TEAM NULL
		return teamManager.unregisterTeam(name);
	}

	@Override
	public @NotNull Scoreboard getScoreboard() {
		return scoreboard;
	}

	public @NotNull TeamManager getTeamManager() {
		return teamManager;
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

		if(!status.equals(Status.PREPARE))
			throw new IllegalStateException("The game doesn't need any more preparation.");

		Optional<World> optional;

		optional = worldGenerator.generate();
		while(!optional.isPresent()) {
			// TODO: MESSAGE BROADCAST ON PREPARE/STARTING
			UHC.logger()
					.log(Level.INFO, "UHC World failed to check requirements, generating again...");
			optional = worldGenerator.generate();
		}

		world = optional.get();

		status = Status.READY;
	}

	@Override
	public void start() throws IllegalStateException {

		if(!status.equals(Status.READY))
			throw new IllegalStateException("The game is not ready yet.");

		if(players.size() < 2)
			throw new IllegalStateException("There must be at least two players to start the game.");

		status = Status.STARTING;

		// DO ASYNC
		Bukkit.getScheduler()
				.runTaskAsynchronously(UHC.plugin(), () -> {

					PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 2400, 100, false, false, false);
					PotionEffect regeneration = new PotionEffect(PotionEffectType.REGENERATION, 2400, 100, false, false, false);
					PotionEffect saturation = new PotionEffect(PotionEffectType.SATURATION, 2400, 100, false, false, false);
					PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 2400, 100, false, false, false);

					Set<Advancement> advancements = new HashSet<>();
					Bukkit.advancementIterator()
							.forEachRemaining(advancement -> {
								if(advancement.getKey()
										.getKey()
										.contains("story"))
									advancements.add(advancement);
							});

					PlayerSpreader spreader = new PlayerSpreader(world, 4000);
					teamManager.getTeams()
							.forEach(team -> spreader.spread(team.getMembersAsArray()));
					teamManager.getTeams()
							.stream()
							.flatMap(team -> team.getMembers()
									.stream())
							.forEach(player -> {

								player.sendTitle("Loading UHC...", null, 10, 70, 20);
								player.addPotionEffect(resistance);
								player.addPotionEffect(regeneration);
								player.addPotionEffect(saturation);
								player.addPotionEffect(blindness);

								//noinspection ConstantConditions
								player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
										.setBaseValue(40);
								player.getInventory()
										.clear();
								player.setGameMode(GameMode.SURVIVAL);
								player.setFoodLevel(20);
								player.setHealth(40);
								player.setLevel(0);

								// REVOKE ALL STORY ADVANCEMENTS
								for(Advancement advancement : advancements) {
									AdvancementProgress progress = player.getAdvancementProgress(advancement);
									progress.getRemainingCriteria()
											.forEach(progress::revokeCriteria);
								}

							});
				});

	}

	private void run() {

	}

	@Override
	public void reset() throws IllegalStateException {

	}

	@Override
	public void stop() {

	}

	@Override
	public @NotNull Game.Status getStatus() {
		return status;
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

}
