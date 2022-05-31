package ovh.excale.mc.utils;

import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import ovh.excale.mc.UHC;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class PlayerSpreader {

	private final Random random;
	private final World world;
	private final int size;

	private final List<CompletableFuture<Void>> futures;

	public PlayerSpreader(World world, int size) {
		this.world = world;
		this.size = size;
		random = new Random(System.currentTimeMillis());
		futures = new LinkedList<>();
	}

	public void spread(Player... players) {
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

		final Location loc = location;

		if(!Bukkit.isPrimaryThread())
			Bukkit.getScheduler()
					.callSyncMethod(UHC.instance(), () -> futures.add(CompletableFuture.allOf(Arrays.stream(players)
							.map(player -> PaperLib.teleportAsync(player, loc))
							.toArray(CompletableFuture[]::new))));
		else
			futures.add(CompletableFuture.allOf(Arrays.stream(players)
					.map(player -> PaperLib.teleportAsync(player, loc))
					.toArray(CompletableFuture[]::new)));

	}

	public void awaitAll() {

		CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
				.join();

	}

}
