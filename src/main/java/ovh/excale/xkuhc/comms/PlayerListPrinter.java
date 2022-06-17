package ovh.excale.xkuhc.comms;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import ovh.excale.xkuhc.core.Gamer;
import ovh.excale.xkuhc.core.GamerHub;
import ovh.excale.xkuhc.xkUHC;

import java.util.function.Function;
import java.util.logging.Logger;

import static com.comphenix.protocol.PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER;
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

		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		Logger log = xkUHC.instance()
				.getLogger();

		for(Gamer gamer : hub.getGamers()) {

			WrappedChatComponent header = WrappedChatComponent.fromLegacyText((headerProvider != null) ? headerProvider.apply(gamer) : "");
			WrappedChatComponent footer = WrappedChatComponent.fromLegacyText((footerProvider != null) ? footerProvider.apply(gamer) : "");

			PacketContainer packet = new PacketContainer(PLAYER_LIST_HEADER_FOOTER);

			packet.getChatComponents()
					.write(0, header)
					.write(1, footer);

			try {

				protocolManager.sendServerPacket(gamer.getPlayer(), packet);

			} catch(Exception e) {
				log.log(WARNING, e.getMessage(), e);
			}

		}

	}

}
