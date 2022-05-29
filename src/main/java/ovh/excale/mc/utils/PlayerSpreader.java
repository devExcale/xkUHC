package ovh.excale.mc.utils;

import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import ovh.excale.mc.UHC;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PlayerSpreader {

	private final Random random;
	private final World world;
	private final int size;

	public PlayerSpreader(World world, int size) {
		this.world = world;
		this.size = size;
		random = new Random(System.currentTimeMillis());
	}

	public boolean spread(Player... players) {
		Block block;
		Location location;

		do {

			int x = random.nextInt(size - 2);
			int z = random.nextInt(size - 2);
			x -= size / 2;
			z -= size / 2;

			block = world.getHighestBlockAt(x, z);

			location = block.getLocation();
			location.setY(location.getY() + 1);

		} while(block.isLiquid());

		final Location finalLocation = location;

		// TODO: check all players teleported
		return Arrays.stream(players)
				.map(player -> PaperLib.teleportAsync(player, finalLocation))
				// Collect futures so that all teleports start processing...
				.collect(Collectors.collectingAndThen(Collectors.toList(), futures ->
						// ... then do another stream to join (blocking) futures and collect all results
						futures.stream()
								.allMatch(CompletableFuture::join)));

	}

}
