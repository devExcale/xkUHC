package ovh.excale.mc.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

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
		int tries = 0;
		Block block;
		Location location;

		do {
			int x = random.nextInt(size - 2), z = random.nextInt(size - 2), y;
			x -= size / 2;
			z -= size / 2;
			y = world.getHighestBlockYAt(x, z);

			block = world.getBlockAt(x, y, z);
			location = block.getLocation();
			location.setY(location.getY() + 1);

		} while(block.isLiquid());

		for(Player player : players)
			player.teleport(location);
	}

}
