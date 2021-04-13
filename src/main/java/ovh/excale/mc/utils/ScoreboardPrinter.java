package ovh.excale.mc.utils;

import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import ovh.excale.mc.uhc.core.Gamer;

import java.util.function.Function;
import java.util.function.Supplier;

public class ScoreboardPrinter {

	private static final int MAX_ROWS = 15;

	private final String[] rowList;
	private final Supplier<String>[] suppliers;

	private final Gamer gamer;
	private final Scoreboard scoreboard;
	private final Objective sidebar;

	public ScoreboardPrinter(Gamer gamer) {
		rowList = new String[MAX_ROWS];
		//noinspection unchecked
		suppliers = new Supplier[MAX_ROWS];
		this.gamer = gamer;
		scoreboard = gamer.getPlayer()
				.getScoreboard();

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
		suppliers[row] = () -> message;
	}

	public void print(int row, Supplier<String> messageSupplier) {
		suppliers[row] = messageSupplier;
	}

	public void print(int row, Function<Gamer, String> messageSupplier) {
		suppliers[row] = () -> messageSupplier.apply(gamer);
	}

	public void clear(int row) {
		suppliers[row] = null;
	}

	public void update() {

		for(int i = 0; i < suppliers.length && i < MAX_ROWS; i++) {

			Supplier<String> supplier = suppliers[i];
			write(i, supplier);

		}

	}

	public void update(Supplier<String>[] globalSupplier) {

		for(int i = 0; i < suppliers.length && i < MAX_ROWS; i++) {

			Supplier<String> supplier = globalSupplier[i] != null ? globalSupplier[i] : suppliers[i];
			write(i, supplier);

		}
	}

	private void write(int rowIndex, Supplier<String> rowSupplier) {

		String row;
		if(rowSupplier == null) {

			StringBuilder rowBuilder = new StringBuilder(" ");
			for(int i = rowIndex; i > 0; i--)
				rowBuilder.append(" ");
			row = rowBuilder.toString();

		} else
			row = rowSupplier.get();

		scoreboard.resetScores(rowList[rowIndex]);
		sidebar.getScore(rowList[rowIndex] = row)
				.setScore(rowIndex);

	}

}
