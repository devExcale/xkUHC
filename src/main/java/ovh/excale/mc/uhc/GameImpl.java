package ovh.excale.mc.uhc;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.UHC;
import ovh.excale.mc.core.Bond;
import ovh.excale.mc.core.BondManager;
import ovh.excale.mc.core.Game;

import java.util.*;
import java.util.stream.Collectors;

public class GameImpl implements Game, BondManager, Listener {

	private final Map<UUID, GamerImpl> gamers;
	private final Map<String, BondImpl> bonds;
	private final Scoreboard scoreboard;

	// TODO: DO_INSOMNIA FALSE IN WORLD
	private World world;
	private boolean frozen;

	public GameImpl() {
		gamers = Collections.synchronizedMap(new HashMap<>());
		bonds = Collections.synchronizedMap(new HashMap<>());
		frozen = false;

		//noinspection ConstantConditions
		scoreboard = Bukkit.getScoreboardManager()
				.getNewScoreboard();

		// SHOW HEARTS ON PLAYER_LIST
		scoreboard.registerNewObjective("health_display", "health", "Hearts", RenderType.HEARTS)
				.setDisplaySlot(DisplaySlot.PLAYER_LIST);

		Bukkit.getPluginManager()
				.registerEvents(this, UHC.plugin());

	}

	private void frozenCheck() throws IllegalStateException {

		if(frozen)
			throw new IllegalStateException("This game won't take any more changes");

	}

	@Override
	public @NotNull Game getGame() {
		return this;
	}

	@Override
	public @NotNull Bond createBond(@NotNull String bondName) throws IllegalStateException, IllegalArgumentException {

		frozenCheck();
		Objects.requireNonNull(bondName);

		BondImpl bond = bonds.get(bondName);

		if(bond != null)
			throw new IllegalArgumentException("A bond with that name already exists");

		bond = new BondImpl(bondName, this);
		bonds.put(bondName, bond);

		return bond;
	}

	@Override
	public void breakBond(@NotNull String bondName) throws IllegalStateException, IllegalArgumentException {

		BondImpl bond = bonds.get(bondName);
		if(bond == null)
			throw new IllegalArgumentException("No bond found for that name");

		bonds.remove(bondName);
		bond.breakBond();

	}

	@Override
	public @Nullable Bond getBond(@Nullable String bondName) {
		return bondName == null ? null : bonds.get(bondName);
	}

	@Override
	public Set<BondImpl> getBonds() {
		return new HashSet<>(bonds.values());
	}

	@Override
	public @NotNull GamerImpl register(@NotNull Player player) throws IllegalStateException, IllegalArgumentException {

		frozenCheck();
		GamerImpl gamer = gamers.get(player.getUniqueId());

		if(gamer != null)
			throw new IllegalArgumentException("This player is already registered");

		gamer = new GamerImpl(player);
		gamers.put(gamer.getUniqueId(), gamer);

		return gamer;
	}

	@Override
	public @Nullable GamerImpl getGamer(@Nullable Player player) {

		return player == null ? null : gamers.get(player.getUniqueId());

	}

	@Override
	public Set<GamerImpl> getGamers() {
		return new HashSet<>(gamers.values());
	}

	@Override
	public Set<Player> getPlayers() {
		return gamers.values()
				.stream()
				.map(GamerImpl::getPlayer)
				.collect(Collectors.toCollection(HashSet::new));
	}

	@Override
	public @NotNull BondManager getBondManager() {
		return this;
	}

	@Override
	public void broadcast(String message) {

		if(message != null)
			for(BondImpl bond : bonds.values())
				for(GamerImpl gamer : bond.getGamers())
					gamer.getPlayer()
							.sendMessage(message);

	}

	@Override
	public void tryStart() throws IllegalStateException {

	}

	private void run() {

	}

	@Override
	public void shutdown() throws IllegalStateException {

		frozenCheck();

		PlayerQuitEvent.getHandlerList()
				.unregister(this);
		PlayerJoinEvent.getHandlerList()
				.unregister(this);
		PlayerDeathEvent.getHandlerList()
				.unregister(this);

	}

	@Override
	public void stop() throws IllegalStateException {

		// TODO: UNREGISTER DEATH EVENT LISTENER

	}

	@Override
	public void freeze() throws IllegalStateException {

		frozenCheck();
		frozen = true;

		for(BondImpl bond : bonds.values())
			try {
				bond.freeze();
			} catch(IllegalStateException ignored) {
			}

	}

	@Override
	public boolean isFrozen() {
		return frozen;
	}

	@Override
	public @NotNull Scoreboard getScoreboard() {
		return scoreboard;
	}

	@EventHandler
	void onPlayerQuit(PlayerQuitEvent event) {

		Player player = event.getPlayer();
		GamerImpl gamer = gamers.get(player.getUniqueId());

		if(gamer != null) {

			// TODO: ON_PLAYER_QUIT DURING GAME

		}
	}

	@EventHandler
	void onPlayerJoin(PlayerJoinEvent event) {

		Player player = event.getPlayer();
		GamerImpl gamer = gamers.get(player.getUniqueId());

		if(gamer != null) {
			gamer.updateReference(player);

			// TODO: ON_PLAYER_JOIN DURING GAME

		}
	}

}
