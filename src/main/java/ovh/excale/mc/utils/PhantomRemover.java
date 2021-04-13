package ovh.excale.mc.utils;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class PhantomRemover implements Listener {

	@EventHandler
	public void onPhantomSpawn(CreatureSpawnEvent event) {

		if(event.getEntity()
				.getType()
				.equals(EntityType.PHANTOM))
			event.setCancelled(true);

	}

}
