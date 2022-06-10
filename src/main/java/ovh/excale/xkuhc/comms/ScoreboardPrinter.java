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

		for(int i = 0; i < rowList.length; i++) {

			StringBuilder s = new StringBuilder(" ");
			for(int j = i; j > 0; j--)
				s.append(" ");

			sidebar.getScore(rowList[i] = s.toString())
					.setScore(i);

		}

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

	// TODO: INVERT INDEX
	private void write(int rowIndex, Function<Gamer, String> rowProvider) {

		String row;
		if(rowProvider == null) {

			StringBuilder rowBuilder = new StringBuilder(" ");
			for(int i = rowIndex; i > 0; i--)
				rowBuilder.append(" ");
			row = rowBuilder.toString();

		} else
			row = rowProvider.apply(gamer);

		scoreboard.resetScores(rowList[rowIndex]);
		sidebar.getScore(rowList[rowIndex] = row)
				.setScore(rowIndex);

	}

}
