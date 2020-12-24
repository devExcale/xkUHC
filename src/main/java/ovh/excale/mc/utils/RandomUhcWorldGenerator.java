package ovh.excale.mc.utils;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.plugin.Plugin;
import ovh.excale.mc.UHC;

import java.io.File;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

public class RandomUhcWorldGenerator {

	private static final Biome[] OCEANS = new Biome[] {
			Biome.OCEAN,
			Biome.COLD_OCEAN,
			Biome.WARM_OCEAN,
			Biome.FROZEN_OCEAN,
			Biome.LUKEWARM_OCEAN,
			Biome.DEEP_OCEAN,
			Biome.DEEP_COLD_OCEAN,
			Biome.DEEP_WARM_OCEAN,
			Biome.DEEP_FROZEN_OCEAN,
			Biome.DEEP_LUKEWARM_OCEAN
	};

	private final Plugin plugin;
	private final Random random;

	public RandomUhcWorldGenerator(Plugin plugin, Long seed) {
		this.plugin = plugin;

		if(seed == null) {
			seed = Instant.now()
					.toEpochMilli();
			seed ^= Bukkit.getIp()
					.hashCode();
		}

		random = new Random();
		random.setSeed(seed);
	}

	public Optional<World> generate() {
		Optional<World> optional = Optional.empty();
		long seed = random.nextLong();

		// TODO: ASYNC WORLD CREATION WITH MULTIVERSE-CORE

		WorldCreator worldCreator = new WorldCreator(seed + ".xkuhc").seed(seed);

		// CREATE WORLD ON PRIMARY BUKKIT THREAD
		if(Bukkit.isPrimaryThread())
			optional = Optional.ofNullable(worldCreator.createWorld());
		else
			try {
				optional = Optional.ofNullable(Bukkit.getScheduler()
						.callSyncMethod(plugin, worldCreator::createWorld)
						.get());
			} catch(Exception ignored) {
			}

		// CHECK IF OCEAN
		optional = optional.filter(world -> {
			int[] coords = new int[] { -48, -32, -16, 0, 16, 32, 48 };
			boolean notOcean = true;

			for(int i = 0; i < coords.length && notOcean; i++)
				for(int j = 0; j < coords.length && notOcean; j++)
					notOcean = !isOcean(world.getBiome(coords[i], coords[j]));

			return notOcean;
		});

		optional.ifPresent(world -> world.setGameRule(GameRule.NATURAL_REGENERATION, false));

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

	public static boolean isOcean(Biome biome) {
		boolean isOcean = false;
		for(int i = 0; i < OCEANS.length && !isOcean; i++)
			isOcean = biome.equals(OCEANS[i]);

		return isOcean;
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
