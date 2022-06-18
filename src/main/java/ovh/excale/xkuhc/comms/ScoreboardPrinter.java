package ovh.excale.xkuhc.comms;

import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import ovh.excale.xkuhc.core.Gamer;

import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Function;

public class ScoreboardPrinter {

	public static final int MAX_ROWS = 15;

	private final String[] rowList;
	private final List<Function<Gamer, String>> rowProviders;

	private final Gamer gamer;
	private final Scoreboard scoreboard;
	private final Objective sidebar;

	public ScoreboardPrinter(Gamer gamer) {
		rowList = new String[MAX_ROWS];
		this.gamer = gamer;
		scoreboard = gamer.getScoreboard();

		rowProviders = new Vector<>(MAX_ROWS);
		for(int i = 0; i < MAX_ROWS; i++)
			rowProviders.add(null);

		sidebar = scoreboard.registerNewObjective("sidebar", "dummy", "xkUHC");
		sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

		for(int i = 0; i < rowList.length; i++)
			sidebar.getScore(rowList[i] = " ".repeat(i + 1))
					.setScore(i);

	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public void print(int row, String message) {
		rowProviders.set(row, (gamer) -> message);
	}

	public void print(int row, Function<Gamer, String> messageSupplier) {
		rowProviders.set(row, messageSupplier);
	}

	public void clear(int row) {
		rowProviders.set(row, null);
	}

	public void update() {

		for(int i = 0; i < rowProviders.size() && i < MAX_ROWS; i++)
			write(i, rowProviders.get(i));

	}

	public void update(List<Function<Gamer, String>> globalProviders) {

		for(int i = 0; i < rowProviders.size() && i < MAX_ROWS; i++) {

			Function<Gamer, String> provider = Optional.ofNullable(globalProviders.get(i))
					.orElse(rowProviders.get(i));
			write(i, provider);

		}
	}

	private void write(int rowIndex, Function<Gamer, String> rowProvider) {

		String row = Optional.ofNullable(rowProvider)
				.map(f -> f.apply(gamer))
				.filter(s -> !s.isBlank())
				.orElseGet(() -> " ".repeat(rowIndex + 1));

		scoreboard.resetScores(rowList[rowIndex]);
		sidebar.getScore(rowList[rowIndex] = row)
				.setScore(rowIndex);

	}

}
