package ovh.excale.xkuhc.commands;

import com.comphenix.packetwrapper.WrapperPlayServerPlayerInfo;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.*;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ovh.excale.xkuhc.world.PlayerSpreader;
import ovh.excale.xkuhc.world.WorldManager;
import ovh.excale.xkuhc.xkUHC;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode.CREATIVE;
import static com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction.*;
import static com.comphenix.protocol.wrappers.WrappedChatComponent.fromLegacyText;
import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings("unused")
@Command("test")
public class TestCommand {

	private static Logger LOG_DO_NOT_REFERENCE = null;

	private static Logger log() {

		if(LOG_DO_NOT_REFERENCE == null)
			LOG_DO_NOT_REFERENCE = xkUHC.instance()
					.getLogger();

		return LOG_DO_NOT_REFERENCE;
	}

	@Default
	public static void nothing(CommandSender commandSender) {

		commandSender.sendMessage("Nothing to see here");

	}

	@Subcommand("gen")
	public static void genWorldAndTp(Player player, @APlayerArgument Player otherPlayer) {

		player.sendMessage("Starting generation");

		World world = new WorldManager(log()).loadSpawn(false)
				.generateUntilClearCenter()
				.applyRules()
				.getWorld();

		player.sendMessage("World generated, commencing teleport");

		PlayerSpreader spreader = new PlayerSpreader(world, 1000);

		spreader.spread(player);
		spreader.spread(otherPlayer);

		player.sendMessage("Teleported");

	}

	@Subcommand("gen")
	public static void genWorldAndTp(Player player) {

		player.sendMessage("Starting generation");

		World world = new WorldManager(log()).loadSpawn(false)
				.generateUntilClearCenter()
				.applyRules()
				.getWorld();

		player.sendMessage("World generated, commencing teleport");

		PlayerSpreader spreader = new PlayerSpreader(world, 1000);
		spreader.spread(player);

		player.sendMessage("Teleported");

	}

	@Subcommand("jump")
	public static void jump(Player player) {

		Bukkit.getScheduler()
				.runTaskAsynchronously(xkUHC.instance(), () -> {

					World world = new WorldManager(log()).loadSpawn(false)
							.generateUntilClearCenter()
							.applyRules()
							.getWorld();

					new PlayerSpreader(world, 2000).spread(player);

				});

	}

	@Subcommand("sound")
	public static void sound(Player player, @ASoundArgument Sound sound, @AFloatArgument float pitch, @AFloatArgument float volume) {
		player.playSound(player, sound, volume, pitch);
	}

	@Subcommand("sound")
	public static void soundNoVolume(Player player, @ASoundArgument Sound sound, @AFloatArgument float pitch) {
		sound(player, sound, pitch, 100f);
	}

	@Subcommand("header")
	public static void editHeader(Player player, @AGreedyStringArgument String header) throws WrapperCommandSyntaxException {

		PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
		packet.getChatComponents()
				.write(0, WrappedChatComponent.fromText(header.replaceAll("\\\\n", "\n")));

		try {

			ProtocolLibrary.getProtocolManager()
					.sendServerPacket(player, packet);

		} catch(InvocationTargetException e) {
			throw CommandAPI.fail(e.getMessage());
		}

	}

	@Subcommand("footer")
	public static void editFooter(Player player, @AGreedyStringArgument String footer) throws WrapperCommandSyntaxException {

		PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
		packet.getChatComponents()
				.write(0, WrappedChatComponent.fromText(""))
				.write(1, WrappedChatComponent.fromText(footer.replaceAll("\\\\n", "\n")));

		try {

			ProtocolLibrary.getProtocolManager()
					.sendServerPacket(player, packet);

		} catch(InvocationTargetException e) {
			throw CommandAPI.fail(e.getMessage());
		}

	}

	@Subcommand("addfakeplayer")
	public static void addFakePlayer(Player player, @AStringArgument String name) {

		WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo();
		packet.setAction(ADD_PLAYER);

		UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(UTF_8));
		WrappedGameProfile profile = new WrappedGameProfile(uuid, name);
		PlayerInfoData playerData = new PlayerInfoData(profile, 0, CREATIVE, WrappedChatComponent.fromText(name));

		packet.setData(List.of(playerData));

		packet.sendPacket(player);

	}

	@Subcommand("removefakeplayer")
	public static void removeFakePlayer(Player player, @AStringArgument String name) {

		WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo();
		packet.setAction(REMOVE_PLAYER);

		UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(UTF_8));
		WrappedGameProfile profile = new WrappedGameProfile(uuid, name);

		packet.setData(List.of(new PlayerInfoData(profile, 0, CREATIVE, null)));

		packet.sendPacket(player);

	}

	@Subcommand("colorfakeplayer")
	public static void colorFakePlayer(Player player, @AStringArgument String name, @AChatColorArgument ChatColor color) {

		WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo();
		packet.setAction(UPDATE_DISPLAY_NAME);

		UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(UTF_8));
		WrappedGameProfile profile = new WrappedGameProfile(uuid, name);
		String displayName = color + name;

		packet.setData(List.of(new PlayerInfoData(profile, 0, CREATIVE, fromLegacyText(displayName))));

		packet.sendPacket(player);

	}

	@Subcommand("getTrueUUID")
	public static void getRealUUID(CommandSender sender, @AOfflinePlayerArgument OfflinePlayer player) {

		sender.sendMessage(player.getUniqueId()
				.toString());

	}

	@Subcommand("getComputedUUID")
	public static void getFakeUUID(CommandSender sender, @AStringArgument String playerName) {

		String namespaced = "OfflinePlayer:" + playerName;
		sender.sendMessage(UUID.nameUUIDFromBytes(namespaced.getBytes(UTF_8))
				.toString());

	}

}
