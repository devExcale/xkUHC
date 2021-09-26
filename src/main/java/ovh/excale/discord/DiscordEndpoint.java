package ovh.excale.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.VoiceChannel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import ovh.excale.mc.UHC;
import ovh.excale.mc.uhc.core.Bond;
import ovh.excale.mc.uhc.core.Gamer;
import ovh.excale.mc.uhc.core.events.GameStartEvent;
import ovh.excale.mc.uhc.core.events.GameStopEvent;
import ovh.excale.mc.uhc.core.events.GamerDeathEvent;
import reactor.core.publisher.Flux;

import java.util.*;

public class DiscordEndpoint implements Listener {

	private static DiscordEndpoint instance;

	public static DiscordEndpoint getInstance() {
		return instance;
	}

	public static DiscordEndpoint open(String token, long guildId) throws IllegalStateException {

		if(instance != null)
			throw new IllegalStateException();

		return instance = new DiscordEndpoint(token, guildId);
	}

	public static void close() {

		if(instance != null) {

			// disable DiscordEndpoint listener
			GameStartEvent.getHandlerList()
					.unregister(instance);
			GameStopEvent.getHandlerList()
					.unregister(instance);
			GamerDeathEvent.getHandlerList()
					.unregister(instance);

			instance.client.logout()
					.block();

			instance = null;
		}

	}

	private final GatewayDiscordClient client;
	private final Guild guild;
	private final Map<Bond, VoiceChannel> channels;
	private final Map<UUID, Member> users;

	private Category category;
	private VoiceChannel mainChannel;
	private VoiceChannel spectChannel;

	private DiscordEndpoint(String token, long guildId) throws RuntimeException {

		users = Collections.synchronizedMap(new HashMap<>());
		channels = Collections.synchronizedMap(new HashMap<>());

		client = DiscordClientBuilder.create(token)
				.build()
				.login()
				.block();

		//noinspection ConstantConditions
		guild = client.getGuildById(Snowflake.of(guildId))
				.block();

		if(guild == null)
			throw new RuntimeException("Couldn't find guild with provided id: " + guildId);

		// REGISTER GAME START EVENT
		Bukkit.getPluginManager()
				.registerEvent(GameStartEvent.class, this, EventPriority.HIGH, (listener, event) -> ((DiscordEndpoint) listener).onGameStart((GameStartEvent) event), UHC.plugin());

		// REGISTER GAME STOP EVENT
		Bukkit.getPluginManager()
				.registerEvent(GameStopEvent.class, this, EventPriority.HIGH, (listener, event) -> ((DiscordEndpoint) listener).onGameStop((GameStopEvent) event), UHC.plugin());

		// REGISTER GAMER DEATH EVENT
		Bukkit.getPluginManager()
				.registerEvent(GamerDeathEvent.class, this, EventPriority.HIGH, (listener, event) -> ((DiscordEndpoint) listener).onGamerDeath((GamerDeathEvent) event),
						UHC.plugin());

	}

	@EventHandler
	private void onGameStart(GameStartEvent gameStartEvent) {

		category = guild.createCategory(categoryCreateSpec -> categoryCreateSpec.setName("xkUHC"))
				.block();

		spectChannel = guild.createVoiceChannel(channelSpec -> channelSpec.setName("Spectators")
						.setParentId(category.getId()))
				.block();

		Set<Bond> bonds = gameStartEvent.getGame()
				.getHub()
				.getBonds();

		for(Bond bond : bonds) {

			VoiceChannel channel = guild.createVoiceChannel(channelSpec -> channelSpec.setName(bond.getName())
							.setParentId(category.getId()))
					.block();

			channels.put(bond, channel);
			for(Gamer gamer : bond.getGamers())
				moveUserToTeamChannel(gamer);

		}

	}

	@EventHandler
	private void onGameStop(GameStopEvent gameStopEvent) {

		Flux.fromIterable(users.values())
				.flatMap(member -> member.edit(memberSpec -> memberSpec.setNewVoiceChannel(mainChannel.getId())))
				.then(category.delete())
				.block();

		DiscordEndpoint.close();

	}

	@EventHandler
	private void onGamerDeath(GamerDeathEvent gamerDeathEvent) {

		Gamer gamer = gamerDeathEvent.getGamer();
		Bond bond = gamer.getBond();

		boolean lastBond = gamer.getGame()
				.getHub()
				.getBonds()
				.stream()
				.filter(Bond::isAlive)
				.count() < 2;

		if(!lastBond)
			moveUserToSpectatorChannel(gamer);

		VoiceChannel channel = channels.get(bond);
		if(!bond.isAlive())
			channel.delete();

	}

	// get user (Discord) from gamer (MC) and move him to team channel
	public void moveUserToTeamChannel(Gamer gamer) {

		Member member = users.get(gamer.getUniqueId());

		member.edit(memberSpec -> memberSpec.setNewVoiceChannel(channels.get(gamer.getBond())
				.getId()));

	}

	// get user (Discord) from gamer (MC) and move him to main channel
	public void moveUserToMainChannel(Gamer gamer) {

		Member member = users.get(gamer.getUniqueId());

		member.edit(memberSpec -> memberSpec.setNewVoiceChannel(mainChannel.getId()));

	}

	// get user (Discord) from gamer (MC) and move him to spectator channel
	public void moveUserToSpectatorChannel(Gamer gamer) {

		Member member = users.get(gamer.getUniqueId());

		member.edit(memberSpec -> memberSpec.setNewVoiceChannel(spectChannel.getId()));

	}

	// bind gamer to Discord user by given Discord user id
	public void bindPlayer(Player player, long userId) throws IllegalArgumentException {

		UUID uuid = player.getUniqueId();
		Member member = client.getMemberById(guild.getId(), Snowflake.of(userId))
				.block();

		if(member == null)
			throw new IllegalArgumentException("Cannot find a discord user with such ID");

		users.put(uuid, member);

	}

	// get Discord user by given gamer
	public User getGamerUser(Gamer gamer) {
		return users.get(gamer.getUniqueId());
	}

}
