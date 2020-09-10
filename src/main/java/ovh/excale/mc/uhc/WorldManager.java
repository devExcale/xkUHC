package ovh.excale.mc.uhc;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.Nullable;
import ovh.excale.mc.UHC;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;

public class WorldManager {

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

	private final String worldName;

	public WorldManager(String worldName) {
		this.worldName = worldName;
	}

	public Optional<World> generate() {
		Optional<World> optional = Optional.empty();

		try {
			optional = Optional.ofNullable(Bukkit.getScheduler()
					.callSyncMethod(UHC.plugin(), new WorldCreator(worldName).seed(worldName.hashCode())::createWorld)
					.get());

			if(optional.isPresent()) {
				World world = optional.get();
				world.setGameRule(GameRule.NATURAL_REGENERATION, false);
				int[] coords = new int[] { -48, -32, -16, 0, 16, 32, 48 };

				outer:
				for(int x : coords)
					for(int z : coords)
						if(isOcean(world.getBiome(x, z))) {
							optional = Optional.empty();
							break outer;
						}
			}

		} catch(Exception ignored) {
		}

		return optional;
	}

	public static void cleanUpWorlds(@Nullable Consumer<Boolean> andThen) {
		Bukkit.getScheduler()
				.runTaskAsynchronously(UHC.plugin(), () -> {
					File[] worlds = Bukkit.getWorldContainer()
							.listFiles((dir, name) -> name.endsWith(".xkuhc"));

					boolean b = true;
					for(File world : worlds)
						b &= deleteFile(world, world);

					if(andThen != null)
						andThen.accept(b);
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
					UHC.plugin()
							.getLogger()
							.warning("Can't delete file " + file1.getName() + " from world " + master.getName());
		return file.delete();
	}

}
