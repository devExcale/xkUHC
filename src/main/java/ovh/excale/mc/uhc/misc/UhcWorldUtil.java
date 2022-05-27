package ovh.excale.mc.uhc.misc;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import ovh.excale.mc.UHC;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;

import static org.bukkit.block.Biome.*;

// TODO: make this class instance-based
public class UhcWorldUtil {

	private static final int[] sampleCoords = new int[] { -48, -32, -16, 0, 16, 32, 48 };

	private static final Biome[] OCEANS = new Biome[] {
			OCEAN, COLD_OCEAN, WARM_OCEAN, FROZEN_OCEAN, LUKEWARM_OCEAN, DEEP_OCEAN, DEEP_COLD_OCEAN, DEEP_FROZEN_OCEAN, DEEP_LUKEWARM_OCEAN,
	};

	private static boolean isOcean(Biome biome) {
		boolean isOcean = false;
		for(int i = 0; i < OCEANS.length && !isOcean; i++)
			isOcean = biome.equals(OCEANS[i]);

		return isOcean;
	}

	private static boolean centerOceanCheck(@NotNull World world) {

		boolean notOcean = true;

		for(int i = 0; i < sampleCoords.length && notOcean; i++)
			for(int j = 0; j < sampleCoords.length && notOcean; j++)
				notOcean = !isOcean(world.getBiome(sampleCoords[i], sampleCoords[j]));

		return !notOcean;
	}

	public static @NotNull Optional<World> generate() {
		Optional<World> optional = Optional.empty();
		long millis = System.currentTimeMillis();
		String name = String.format("%x", millis) + ".xkuhc";

		WorldCreator worldCreator = new WorldCreator(name).seed(millis);
		World world = null;

		if(Bukkit.isPrimaryThread())
			world = worldCreator.createWorld();
		else
			try {
				world = Bukkit.getScheduler()
						.callSyncMethod(UHC.plugin(), worldCreator::createWorld)
						.get();
			} catch(Exception e) {
				UHC.logger()
						.log(Level.WARNING, e.getMessage(), e);
			}

		if(world != null && !centerOceanCheck(world)) {

			world.setGameRule(GameRule.NATURAL_REGENERATION, false);
			world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
			world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
			world.setWeatherDuration(0);

			optional = Optional.of(world);
		}

		return optional;
	}

	public static void purgeWorlds(Consumer<Integer> then) {
		Bukkit.getScheduler()
				.runTaskAsynchronously(UHC.plugin(), () -> {

					File[] worlds = Bukkit.getWorldContainer()
							.listFiles((dir, name) -> name.endsWith(".xkuhc") && Bukkit.getWorld(name) == null);

					int i = 0;
					for(File world : worlds)
						if(deleteFile(world, world))
							i++;

					if(then != null)
						then.accept(i);

				});

	}

	private static boolean deleteFile(File master, File file) {
		File[] files = file.listFiles();
		if(files != null)
			for(File file1 : files)
				if(!deleteFile(master, file1))
					UHC.logger()
							.warning("Can't delete file " + file1.getName() + " from world " + master.getName());
		return file.delete();
	}

}
