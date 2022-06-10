package ovh.excale.xkuhc.comms;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import ovh.excale.xkuhc.xkUHC;
import ovh.excale.xkuhc.core.Gamer;

import java.util.*;
import java.util.function.Function;

public class ScoreboardProcessor {

	private final Set<ScoreboardPrinter> printers;
	private final List<Function<Gamer, String>> providers;

	private BukkitTask task;

	public ScoreboardProcessor() {

		printers = Collections.synchronizedSet(new HashSet<>());
		providers = new Vector<>(ScoreboardPrinter.MAX_ROWS);
		task = null;

		for(int i = 0; i < ScoreboardPrinter.MAX_ROWS; i++)
			providers.add(null);

	}

	public void stop() {
		if(task != null)
			task.cancel();
	}

	public boolean isRunning() {
		return task != null && !task.isCancelled();
	}

	public void compute() {

		synchronized(printers) {

			for(ScoreboardPrinter printer : printers)
				printer.update(providers);

		}

	}

	public void track(ScoreboardPrinter printer) {
		printers.add(printer);

		if(!isRunning())
			task = Bukkit.getScheduler()
					.runTaskTimerAsynchronously(xkUHC.instance(), this::compute, 0L, 20L);

	}

	public void untrack(ScoreboardPrinter printer) {
		printers.remove(printer);

		if(printers.isEmpty() && isRunning())
			stop();

	}

	public void untrackAll() {
		printers.clear();

		if(isRunning())
			stop();

	}

	public void print(int row, String message) {
		providers.set(row, gamer -> message);
	}

	public void print(int row, Function<Gamer, String> messageProvider) {
		providers.set(row, messageProvider);
	}

	public void clear(int row) {
		providers.set(row, null);
	}

}
