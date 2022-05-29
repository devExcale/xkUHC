package ovh.excale.mc.uhc.world;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.bukkit.block.Biome.*;

public class OceanRemoverBiomeProvider extends BiomeProvider {

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

	private final BiomeProvider rootProvider;

	public OceanRemoverBiomeProvider(@NotNull BiomeProvider rootProvider) {
		this.rootProvider = Objects.requireNonNull(rootProvider);
	}

	@NotNull
	@Override
	public Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {

		Biome biome = rootProvider.getBiome(worldInfo, x, y, z);

		if(OCEAN_BIOMES.contains(biome))
			biome = FOREST;

		return biome;
	}

	@NotNull
	@Override
	public List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {

		List<Biome> biomes = rootProvider.getBiomes(worldInfo);
		biomes.removeAll(OCEAN_BIOMES);

		return biomes;
	}

}
