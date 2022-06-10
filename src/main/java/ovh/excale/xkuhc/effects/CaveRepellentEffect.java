package ovh.excale.xkuhc.effects;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import ovh.excale.xkuhc.xkUHC;
import ovh.excale.xkuhc.comms.MessageBundles;

import java.time.Instant;
import java.util.List;

import static org.bukkit.ChatColor.ITALIC;
import static org.bukkit.Material.*;

public class CaveRepellentEffect {

	public static NamespacedKey key() {
		return new NamespacedKey(xkUHC.instance(), "cave_repellent");
	}

	public static ShapelessRecipe recipe() {

		MessageBundles msg = xkUHC.instance()
				.getMessages();

		ItemStack tear = new ItemStack(GHAST_TEAR);
		ItemMeta meta = tear.getItemMeta();

		//noinspection ConstantConditions
		meta.setDisplayName(ITALIC + msg.game("repellent.name"));
		meta.setLore(List.of(msg.game("repellent.description")));

		tear.setItemMeta(meta);

		return new ShapelessRecipe(key(), tear).addIngredient(DIAMOND)
				.addIngredient(SUGAR)
				.addIngredient(POTION)
				.addIngredient(EGG);

	}

	private final Instant startTime;
	private long duration;

	public CaveRepellentEffect(long duration) {
		startTime = Instant.now();
		this.duration = duration;
	}

	public Instant getStartTime() {
		return startTime;
	}

	public long getDuration() {
		return duration;
	}

	public void addDuration(long duration) {
		this.duration += duration;
	}

}
