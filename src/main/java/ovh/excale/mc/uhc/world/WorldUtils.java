package ovh.excale.mc.uhc.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import ovh.excale.mc.UHC;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Stream;

import static org.bukkit.GameRule.*;
import static org.bukkit.block.Biome.*;

// TODO: make this class instance-based
public class WorldUtils {

	private static final int[] sampleCoords = new int[] { -48, -32, -16, 0, 16, 32, 48 };

	private static final Biome[] OCEANS = new Biome[] {
			OCEAN,
			COLD_OCEAN,
			WARM_OCEAN,
			FROZEN_OCEAN,
			LUKEWARM_OCEAN,
			DEEP_OCEAN,
			DEEP_COLD_OCEAN,
			DEEP_FROZEN_OCEAN,
			DEEP_LUKEWARM_OCEAN,
	};

	private static boolean isOcean(Biome biome) {
		boolean isOcean = false;
		for(int i = 0; i < OCEANS.length && !isOcean; i++)
			isOcean = biome.equals(OCEANS[i]);

		return isOcean;
	}

	private static boolean notOcean(@NotNull World world) {

		boolean notOcean = true;

		for(int i = 0; i < sampleCoords.length && notOcean; i++)
			for(int j = 0; j < sampleCoords.length && notOcean; j++)
				notOcean = !isOcean(world.getBiome(sampleCoords[i], 64, sampleCoords[j]));

		return notOcean;
	}

	public static @NotNull Optional<World> generate() {
		Optional<World> optional = Optional.empty();
		long millis = System.currentTimeMillis();
		String name = "%x.xkuhc".formatted(millis);

		WorldCreator worldCreator = new WorldCreator(name).seed(millis);
		World world = null;

		if(Bukkit.isPrimaryThread())
			world = worldCreator.createWorld();
		else
			try {
				world = Bukkit.getScheduler()
						.callSyncMethod(UHC.instance(), worldCreator::createWorld)
						.get();
			} catch(Exception e) {
				UHC.log()
						.log(Level.WARNING, e.getMessage(), e);
			}

		if(world != null && notOcean(world)) {

			world.setGameRule(SPECTATORS_GENERATE_CHUNKS, false);
			world.setGameRule(NATURAL_REGENERATION, false);
			world.setGameRule(SHOW_DEATH_MESSAGES, false);
			world.setGameRule(DO_WEATHER_CYCLE, false);
			world.setGameRule(DO_INSOMNIA, false);
			world.setWeatherDuration(0);

			optional = Optional.of(world);
		}

		return optional;
	}

	public static void purgeWorlds(Consumer<Integer> then) {
		Bukkit.getScheduler()
				.runTaskAsynchronously(UHC.instance(), () -> {

					File[] worlds = Bukkit.getWorldContainer()
							.listFiles((dir, name) -> name.endsWith(".xkuhc") && Bukkit.getWorld(name) == null);

					int count = (int) Optional.ofNullable(worlds)
							.stream()
							.flatMap(Stream::of)
							.map(worldFolder -> deleteFile(worldFolder, worldFolder))
							.filter(bool -> bool)
							.count();

					if(then != null)
						then.accept(count);

				});

	}

	private static boolean deleteFile(File master, File file) {
		File[] files = file.listFiles();
		if(files != null)
			for(File file1 : files)
				if(!deleteFile(master, file1))
					UHC.log()
							.warning("Can't delete file " + file1.getName() + " from world " + master.getName());
		return file.delete();
	}

}
