package ovh.excale.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.VoiceChannel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ovh.excale.mc.UHC;
import ovh.excale.mc.uhc.Game;
import ovh.excale.mc.uhc.core.Bond;
import ovh.excale.mc.uhc.core.Gamer;
import ovh.excale.mc.uhc.core.GamerHub;
import ovh.excale.mc.uhc.core.events.GameStartEvent;
import ovh.excale.mc.uhc.core.events.GameStopEvent;
import ovh.excale.mc.uhc.core.events.GamerDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class DiscordEndpoint implements Listener {

    private final GatewayDiscordClient client;
    private final Guild guild;
    private Category category;
    private Map<Bond, VoiceChannel> voiceChannels = new HashMap<>();
    private int channelPos;
    private VoiceChannel mainChannel;
    private VoiceChannel specChannel;

    private static DiscordEndpoint instance;
    // false - disabled; true - enabled;
    private boolean status = false;

    public static DiscordEndpoint getInstance() {
        return instance;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public GatewayDiscordClient getClient() {
        return client;
    }

    @EventHandler
    private void onGameStart(GameStartEvent gameStartEvent) {
        if (status) {
            category = guild.createCategory(categoryCreateSpec ->
                            categoryCreateSpec.setName("UHC")
                                    .setPosition(2))
                    .block();
            mainChannel = guild.createVoiceChannel(voiceChannelCreateSpec ->
                            voiceChannelCreateSpec.setName("Hub")
                                    .setPosition(1)
                                    .setParentId(category.getId()))
                    .block();
            specChannel = guild.createVoiceChannel(voiceChannelCreateSpec ->
                            voiceChannelCreateSpec.setName("Spectators")
                                    .setPosition(2)
                                    .setParentId(category.getId()))
                    .block();
            channelPos = 3;
            Game game = gameStartEvent.getGame();
            GamerHub hub = game.getHub();
            Set<Bond> bonds = hub.getBonds();
            bonds.forEach(bond -> {
                VoiceChannel channel = guild.createVoiceChannel(voiceChannelCreateSpec ->
                                voiceChannelCreateSpec.setName(bond.getName())
                                        .setPosition(channelPos))
                        .block();
                voiceChannels.put(bond, channel);
                // TODO: move users to team channels
                for (Gamer gamer : bond.getGamers()) {
                    moveUserToTeamChannel(gamer);
                }
                channelPos++;
            });
        }
    }

    @EventHandler
    private void onGameStop(GameStopEvent gameStopEvent) {
        // TODO: disconnect bot
        // TODO: move users to main channel
        // TODO: think about if main channel outside of category or never delete category or never create main channel
        if (status)
            category.delete();
        voiceChannels.forEach((bond, voiceChannel) -> bond.getGamers().forEach(this::moveUserToMainChannel));
        // move users from spectator channel to main channel
        specChannel.getVoiceStates()
                .flatMap(VoiceState::getMember)
                .subscribe(member ->
                        member.edit(guildMemberEditSpec ->
                                guildMemberEditSpec.setNewVoiceChannel(mainChannel.getId())));
    }

    @EventHandler
    private void onGamerDeath(GamerDeathEvent gamerDeathEvent) {
        if (status) {
            Gamer gamer = gamerDeathEvent.getGamer();
            Bond bond = gamer.getBond();
            //TODO: move user to spectator channel
            moveUserToSpectatorChannel(gamer);
            VoiceChannel channel = voiceChannels.get(bond);
            if (bond.getGamers()
                    .stream()
                    .noneMatch(Gamer::isAlive))
                channel.delete();
        }
    }

    public void moveUserToTeamChannel(Gamer gamer) {
        // TODO: get user (Discord) from gamer (MC) and move him to team channel

    }

    public void moveUserToMainChannel(Gamer gamer) {
        // TODO: get user (Discord) from gamer (MC) and move him to main channel

    }

    public void moveUserToSpectatorChannel(Gamer gamer) {
        // TODO: get user (Discord) from gamer (MC) and move him to spectator channel

    }

    /*
    TODO: create map gamer (MC) - user (Discord)
    TODO: create method for get user (Discord) from gamer (MC)
    public User getGamerUser(Gamer gamer) {

    }
    */


    public DiscordEndpoint(long guildID) {
        client = DiscordClientBuilder
                .create("")
                .build()
                .login()
                .block();
        if (client != null) {
            client.onDisconnect()
                    .block();
            guild = client.getGuildById(Snowflake.of(guildID)).block();
        } else {
            UHC.logger()
                    .log(Level.SEVERE, "Error, couldn't connect to discord guild!");
            guild = null;
        }
    }
}
