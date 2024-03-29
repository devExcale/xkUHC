package ovh.excale.xkuhc.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.shard.GatewayBootstrap;
import discord4j.core.spec.GuildMemberEditSpec;
import discord4j.core.spec.VoiceChannelCreateSpec;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ovh.excale.xkuhc.events.gamer.GamerDeathEvent;
import ovh.excale.xkuhc.xkUHC;
import ovh.excale.xkuhc.core.Bond;
import ovh.excale.xkuhc.core.Gamer;
import ovh.excale.xkuhc.events.game.GameStartEvent;
import ovh.excale.xkuhc.events.game.GameStopEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static discord4j.core.object.entity.channel.Channel.Type.GUILD_VOICE;

@SuppressWarnings("BlockingMethodInNonBlockingContext")
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
	private final Map<String, VoiceChannel> bondChannels;
	private final Map<UUID, Member> users;

	private Category category;
	private VoiceChannel mainChannel;
	private VoiceChannel spectChannel;

	private DiscordEndpoint(String token, long guildId) throws RuntimeException {

		users = Collections.synchronizedMap(new HashMap<>());
		bondChannels = Collections.synchronizedMap(new HashMap<>());

		category = null;
		mainChannel = null;
		spectChannel = null;

		client = DiscordClientBuilder.create(token)
				.build()
				.login()
				.flatMap(gatewayDiscordClient -> GatewayBootstrap.create(gatewayDiscordClient.rest())
						.setEnabledIntents(IntentSet.of(Intent.GUILDS, Intent.GUILD_MEMBERS, Intent.GUILD_VOICE_STATES))
						.login())
				.block();

		//noinspection ConstantConditions
		guild = client.getGuildById(Snowflake.of(guildId))
				.block();

		if(guild == null)
			throw new RuntimeException("Couldn't find guild with provided id: " + guildId);

		Bukkit.getPluginManager()
				.registerEvents(this, xkUHC.instance());

	}

	public boolean hasMainChannel() {
		return mainChannel != null;
	}

	@EventHandler
	private void onGameStart(GameStartEvent gameStartEvent) {

		category = Objects.requireNonNull(guild.createCategory("xkUHC")
				.block());

		spectChannel = guild.createVoiceChannel(VoiceChannelCreateSpec.builder()
						.name("Spectators")
						.parentId(category.getId())
						.build())
				.block();

		Set<Bond> bonds = gameStartEvent.getGame()
				.getHub()
				.getBonds();

		for(Bond bond : bonds) {

			VoiceChannel channel = guild.createVoiceChannel(VoiceChannelCreateSpec.builder()
							.name(bond.getName())
							.parentId(category.getId())
							.build())
					.block();

			bondChannels.put(bond.getName(), channel);
			for(Gamer gamer : bond.getGamers())
				moveUserToTeamChannel(gamer);

		}

	}

	@EventHandler
	private void onGameStop(GameStopEvent gameStopEvent) {

		for(Member member : users.values()) {
			VoiceChannel channel = member.getVoiceState()
					.flatMap(VoiceState::getChannel)
					.block();
			System.out.println(member.getDisplayName() + ": " + (channel != null ? channel.getName() : null));
		}

		// Move all users
		Flux.fromIterable(users.values())
				.filter(member -> member.getVoiceState()
						.flatMap(VoiceState::getChannel)
						.block() != null)
				.flatMap(this::moveUserToMainChannel)
				.blockLast();

		// Delete all channels
		Flux.fromIterable(bondChannels.values())
				.flatMap(Channel::delete)
				.then(spectChannel.delete())
				.then(category.delete())
				.block();

		bondChannels.clear();
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

		if(!lastBond) {
			moveUserToSpectatorChannel(gamer);
			if(!bond.isAlive())
				deleteBondChannel(bond);
		}

	}

	public void deleteBondChannel(Bond bond) {

		Channel channel = bondChannels.get(bond.getName());
		if(channel != null) {
			bondChannels.remove(bond.getName());
			channel.delete()
					.block();
		}

	}

	// get user (Discord) from gamer (MC) and move him to team channel
	public void moveUserToTeamChannel(Gamer gamer) {

		Bond bond = gamer.getBond();
		Member member = users.get(gamer.getUniqueId());

		if(member == null)
			return;

		member.edit(GuildMemberEditSpec.builder()
						.newVoiceChannelOrNull(bondChannels.get(bond.getName())
								.getId())
						.build())
				.block();

	}

	// get user (Discord) from gamer (MC) and move him to main channel
	public void moveUserToMainChannel(Gamer gamer) {

		Member member = users.get(gamer.getUniqueId());
		member.edit(GuildMemberEditSpec.builder()
						.newVoiceChannelOrNull(mainChannel.getId())
						.build())
				.block();

	}

	// move user (Discord) to main channel
	public Mono<Member> moveUserToMainChannel(Member member) {

		return member.edit(GuildMemberEditSpec.builder()
				.newVoiceChannelOrNull(mainChannel.getId())
				.build());

	}

	// get user (Discord) from gamer (MC) and move him to spectator channel
	public void moveUserToSpectatorChannel(Gamer gamer) {

		Member member = users.get(gamer.getUniqueId());
		member.edit(GuildMemberEditSpec.builder()
						.newVoiceChannelOrNull(spectChannel.getId())
						.build())
				.block();

	}

	public VoiceChannel setMainChannel(long channelId) throws IllegalArgumentException {

		Channel channel = guild.getChannelById(Snowflake.of(channelId))
				.block();

		if(channel == null)
			throw new IllegalArgumentException("Couldn't find a channel with such ID");

		if(channel.getType() != GUILD_VOICE)
			throw new IllegalArgumentException("Illegal channel type");

		return mainChannel = (VoiceChannel) channel;
	}

	// link gamer to Discord user by given Discord user id
	public Member linkPlayer(Player player, long userId) throws IllegalArgumentException {

		UUID uuid = player.getUniqueId();
		Member member = client.getMemberById(guild.getId(), Snowflake.of(userId))
				.block();

		if(member == null)
			throw new IllegalArgumentException("Cannot find a discord user with such ID");

		users.put(uuid, member);

		return member;
	}

	public Set<UUID> getLinkedPlayers() {
		return Set.copyOf(users.keySet());
	}

	// get Discord user by given gamer
	public User getGamerUser(Gamer gamer) {
		return users.get(gamer.getUniqueId());
	}

}
