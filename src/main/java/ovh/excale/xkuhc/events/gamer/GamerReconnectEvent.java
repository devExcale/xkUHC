package ovh.excale.xkuhc.events.gamer;

import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.xkuhc.core.Gamer;

@Getter
public class GamerReconnectEvent extends GamerEvent {

	private static final HandlerList handlers = new HandlerList();

	public GamerReconnectEvent(Gamer gamer, PlayerJoinEvent parentEvent) {
		super(gamer, parentEvent);

		parentEventCheck(event -> ((PlayerJoinEvent) event).getPlayer());

	}

	@Override
	public @Nullable PlayerJoinEvent getParentEvent() {
		return (PlayerJoinEvent) super.getParentEvent();
	}

	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}

}
