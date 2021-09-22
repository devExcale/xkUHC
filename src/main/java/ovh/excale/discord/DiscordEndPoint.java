package ovh.excale.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.CategoryCreateSpec;
import discord4j.core.spec.VoiceChannelCreateSpec;
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
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class DiscordEndPoint implements Listener {
    private final GatewayDiscordClient client;
    private final Guild guild;
    private Category category;
    private Map<Bond, VoiceChannel> voiceChannels;

    @EventHandler
    private void onGameStart(GameStartEvent gameStartEvent) {
        CategoryCreateSpec categoryCreateSpec = new CategoryCreateSpec()
                .setName("UHC")
                .setPosition(2);
        // C'ho provato xd dopo cerco meglio mi sembra sbagliato
        Mono<Category> categoryMono = guild.createCategory(categoryCreateSpec1 -> Mono.just(categoryCreateSpec));
        category = categoryMono.block();
        Game game = gameStartEvent.getGame();
        GamerHub hub = game.getHub();
        Set<Bond> bonds = hub.getBonds();
        bonds.forEach(bond -> {
            VoiceChannelCreateSpec voiceChannelCreateSpec = new VoiceChannelCreateSpec()
                    .setName(bond.getName());
            // C'ho provato xd x2
            Mono<VoiceChannel> channel = guild.createVoiceChannel(voiceChannelCreateSpec1 -> Mono.just(voiceChannelCreateSpec));
            voiceChannels.put(bond, channel.block());
            //TODO: spostare users
            for (Gamer gamer : bond.getGamers()) {
                moveUserToTeamChannel(gamer);
            }
        });
    }

    @EventHandler
    private void onGameStop(GameStopEvent gameStopEvent) {
        category.delete();
    }

    @EventHandler
    private void onGamerDeath(GamerDeathEvent gamerDeathEvent) {
        Gamer gamer = gamerDeathEvent.getGamer();
        Bond bond = gamer.getBond();
        //TODO: spostare user
        moveUserToSpectatorChannel(gamer);
        VoiceChannel channel = voiceChannels.get(bond);
        if (bond.getGamers().stream().noneMatch(Gamer::isAlive))
            channel.delete();

    }

    public void moveUserToTeamChannel(Gamer gamer) {

    }

    public void moveUserToMainChannel(Gamer gamer) {

    }

    public void moveUserToSpectatorChannel(Gamer gamer) {

    }

    /* TODO: fare associazione gamer (MC) - user (Discord)
    public User getGamerUser(Gamer gamer) {

    }

     */


    public DiscordEndPoint(long guildID) {
        // Pure qua c'ho provato me puzza un pò cerco meglio più tardi/sta sera (Il bot l'ho creato su ds developers)
        client = DiscordClientBuilder.create("ODkwMTcwMTkyMDYyMjE0MTc2.YUr5mA.HZ6dzf_ZLe9o8ITTpNUEqk-C_rg").build()
                .login()
                .block();
        if (client != null) {
            client.onDisconnect().block();
            guild = client.getGuildById(Snowflake.of(guildID)).block();
        } else {
            UHC.logger()
                    .log(Level.SEVERE, "Error, coulnd't connect to discord guild!");
            guild = null;
        }




        /*
        //TODO: login discord bot
        GatewayDiscordClient client;

        client = DiscordClientBuilder.create("ODkwMTcwMTkyMDYyMjE0MTc2.YUr5mA.HZ6dzf_ZLe9o8ITTpNUEqk-C_rg").build()
                .login()
                .block();


         */
    }
}
