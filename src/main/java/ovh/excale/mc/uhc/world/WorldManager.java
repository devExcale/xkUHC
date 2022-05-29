package ovh.excale.mc.uhc.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.UHC;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.bukkit.GameRule.*;
import static org.bukkit.block.Biome.*;

public class WorldManager implements Listener {

	private static final String NAME_FORMAT = "%x.xkuhc";

	private static final Set<Biome> OCEAN_BIOMES;

	static {

		OCEAN_BIOMES = new HashSet<>();
		OCEAN_BIOMES.add(OCEAN);
		OCEAN_BIOMES.add(COLD_OCEAN);
		OCEAN_BIOMES.add(WARM_OCEAN);
		OCEAN_BIOMES.add(FROZEN_OCEAN);
		OCEAN_BIOMES.add(LUKEWARM_OCEAN);
		OCEAN_BIOMES.add(DEEP_OCEAN);
		OCEAN_BIOMES.add(DEEP_COLD_OCEAN);
		OCEAN_BIOMES.add(DEEP_FROZEN_OCEAN);
		OCEAN_BIOMES.add(DEEP_LUKEWARM_OCEAN);

	}

	private boolean loadSpawn;

	private World world;
	private String worldName;

	public WorldManager() {

		loadSpawn = true;
		world = null;

	}

	public WorldManager loadSpawn(boolean loadSpawn) {

		this.loadSpawn = loadSpawn;

		return this;
	}

	public WorldManager generateOnce() {

		long millis = System.currentTimeMillis();
		worldName = NAME_FORMAT.formatted(millis);
		WorldCreator worldCreator = new WorldCreator(worldName).seed(millis);

		if(!loadSpawn)
			Bukkit.getPluginManager()
					.registerEvents(this, UHC.instance());

		world = worldCreator.createWorld();

		return this;
	}

	public WorldManager generateUntilClearCenter() {

		do {

			generateOnce();

		} while(hasOceanInSquareCenter(3));

		return this;
	}

	public boolean hasOceanInSquareCenter(int chunkRadius) {

		return world != null && IntStream.range(-chunkRadius, chunkRadius)
				.mapToObj(x -> IntStream.range(-chunkRadius, chunkRadius)
						.mapToObj(z -> world.getEmptyChunkSnapshot(x * 16, z * 16, true, false)))
				.flatMap(stream -> stream)
				.map(chunkSnapshot -> chunkSnapshot.getBiome(0, 64, 0))
				.anyMatch(OCEAN_BIOMES::contains);

	}

	public WorldManager applyRules() {

		world.setGameRule(SPECTATORS_GENERATE_CHUNKS, false);
		world.setGameRule(NATURAL_REGENERATION, false);
		world.setGameRule(SHOW_DEATH_MESSAGES, false);
		world.setGameRule(DO_WEATHER_CYCLE, false);
		world.setGameRule(DO_INSOMNIA, false);
		world.setWeatherDuration(0);

		return this;
	}

	@Nullable
	public World getWorld() {
		return world;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void worldInit(WorldInitEvent event) {

		World eWorld = event.getWorld();

		if(worldName.equals(eWorld.getName())) {
			eWorld.setKeepSpawnInMemory(false);
			WorldInitEvent.getHandlerList()
					.unregister(this);
		}

	}

}
