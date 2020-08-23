package ovh.excale.mc.uhc;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import ovh.excale.mc.UHC;

import java.io.File;
import java.util.Optional;

public class WorldManager {

	private final String worldName;

	public WorldManager(String worldName) {
		this.worldName = worldName;
	}

	public Optional<World> generate() {
		Optional<World> optional = Optional.empty();
		try {
			optional = Optional.ofNullable(Bukkit.getScheduler()
					.callSyncMethod(UHC.plugin(), new WorldCreator(worldName).seed(worldName
							.hashCode())::createWorld)
					.get());
		} catch(Exception ignored) {
		}

		optional.ifPresent(world1 -> world1.setGameRule(GameRule.NATURAL_REGENERATION, false));
		return optional;
	}

	public static void cleanUpWorlds(Runnable andThen) {

		Bukkit.getScheduler().runTaskAsynchronously(UHC.plugin(), () -> {
			Bukkit.getWorlds().forEach(world1 -> {

				String[] split = world1.getName().split("\\.");

				if(split[split.length - 1].equals("xkuhc"))
					deleteFile(world1.getWorldFolder());
			});

			andThen.run();
		});

	}

	private static boolean deleteFile(File file) {
		File[] files = file.listFiles();
		if(files != null)
			for(File file1 : files)
				if(!deleteFile(file))
					System.out.println("[xkUHC cleanup] Can't delete " + file1.getName());
		return file.delete();
	}

}
