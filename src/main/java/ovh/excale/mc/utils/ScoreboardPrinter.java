package ovh.excale.mc.utils;

import org.bukkit.scoreboard.Scoreboard;
import ovh.excale.mc.core.Gamer;

import java.util.function.Function;
import java.util.function.Supplier;

public class ScoreboardPrinter {

	private static final int MIN_ROW = 0;
	private static final int MAX_ROW = 14;

	private final String[] rowList;
	private final Supplier<String>[] suppliers;

	private final Gamer gamer;
	private final Scoreboard scoreboard;

	public ScoreboardPrinter(Gamer gamer) {
		rowList = new String[MAX_ROW + 1];
		//noinspection unchecked
		suppliers = new Supplier[MAX_ROW + 1];
		this.gamer = gamer;
		scoreboard = gamer.getPlayer()
				.getScoreboard();
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public void print(int row, String message) {
		suppliers[row] = () -> message;
	}

	public void print(int row, Supplier<String> messageSupplier) {
		suppliers[row] = messageSupplier;
	}

	public void print(int row, Function<Gamer, String> messageSupplier) {
		suppliers[row] = () -> messageSupplier.apply(gamer);
	}

}
