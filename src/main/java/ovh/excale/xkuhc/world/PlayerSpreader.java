package ovh.excale.xkuhc.world;

import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import ovh.excale.xkuhc.xkUHC;

import java.util.Random;

public class PlayerSpreader {

	private final Random random;
	private final World world;
	private final int size;

	public PlayerSpreader(World world, int size) {
		this.world = world;
		this.size = size;
		random = new Random(System.currentTimeMillis());
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
					.callSyncMethod(xkUHC.instance(), () -> {

						for(Player player : players)
							PaperLib.teleportAsync(player, loc);

						return null;
					});
		else
			for(Player player : players)
				PaperLib.teleportAsync(player, loc);

	}

}
