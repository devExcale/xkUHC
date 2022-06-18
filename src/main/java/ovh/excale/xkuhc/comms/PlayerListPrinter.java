package ovh.excale.xkuhc.comms;

import com.comphenix.packetwrapper.WrapperPlayServerPlayerListHeaderFooter;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import ovh.excale.xkuhc.core.Gamer;
import ovh.excale.xkuhc.core.GamerHub;
import ovh.excale.xkuhc.xkUHC;

import java.util.function.Function;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

public class PlayerListPrinter {

	private final GamerHub hub;

	private Function<Gamer, String> headerProvider;
	private Function<Gamer, String> footerProvider;

	private BukkitTask task;

	public PlayerListPrinter(GamerHub hub) {

		this.hub = hub;

		headerProvider = null;
		footerProvider = null;
		task = null;

	}

	public void header(@Nullable Function<Gamer, String> msgProvider) {
		headerProvider = msgProvider;
	}

	public void footer(@Nullable Function<Gamer, String> msgProvider) {
		footerProvider = msgProvider;
	}

	public void activate() {

		task = Bukkit.getScheduler()
				.runTaskTimer(xkUHC.instance(), this::update, 0L, 20L);

	}

	public void deactivate() {

		headerProvider = null;
		footerProvider = null;

		update();

		if(task != null && !task.isCancelled()) {

			task.cancel();
			task = null;

		}

	}

	public void update() {

		for(Gamer gamer : hub.getGamers()) {

			WrappedChatComponent header = WrappedChatComponent.fromLegacyText((headerProvider != null) ? headerProvider.apply(gamer) : "");
			WrappedChatComponent footer = WrappedChatComponent.fromLegacyText((footerProvider != null) ? footerProvider.apply(gamer) : "");

			WrapperPlayServerPlayerListHeaderFooter packet = new WrapperPlayServerPlayerListHeaderFooter();

			packet.setHeader(header);
			packet.setFooter(footer);

			try {

				packet.sendPacket(gamer.getPlayer());

			} catch(Exception e) {
				xkUHC.instance()
						.getLogger()
						.log(WARNING, e.getMessage(), e);
			}

		}

	}

}
