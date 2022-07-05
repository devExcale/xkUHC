package ovh.excale.xkuhc.events.gamer;

import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.xkuhc.core.Gamer;

import java.util.Optional;
import java.util.function.Function;

@Getter
public class GamerEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	protected final Gamer gamer;

	protected final Player player;

	@Nullable
	protected final Event parentEvent;

	public GamerEvent(Gamer gamer) {
		this(gamer, null);
	}

	public GamerEvent(Gamer gamer, @Nullable Event parentEvent) {
		this.gamer = gamer;
		this.parentEvent = parentEvent;
		player = gamer.getPlayer();
	}

	protected boolean gamerIsInParentEvent(Function<? super Event, OfflinePlayer> playerExtractor) {

		return Optional.ofNullable(parentEvent)
				.map(playerExtractor)
				.map(OfflinePlayer::getUniqueId)
				.map(gamer.getUniqueId()::equals)
				.orElse(false);

	}

	protected void parentEventCheck(Function<? super Event, OfflinePlayer> playerExtractor) throws IllegalArgumentException {

		if(!gamerIsInParentEvent(playerExtractor))
			throw new IllegalArgumentException("ParentEvent isn't related to Gamer");

	}

	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}

}
