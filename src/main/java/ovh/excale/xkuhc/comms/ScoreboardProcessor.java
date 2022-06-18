package ovh.excale.xkuhc.comms;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ovh.excale.xkuhc.core.Game.Phase;
import ovh.excale.xkuhc.core.GameAccessory;
import ovh.excale.xkuhc.core.Gamer;
import ovh.excale.xkuhc.xkUHC;

import java.util.*;
import java.util.function.Function;

public class ScoreboardProcessor implements GameAccessory {

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

	@Override
	public void enable() {

		if(isEnabled())
			return;

		task = Bukkit.getScheduler()
				.runTaskTimerAsynchronously(xkUHC.instance(), this::compute, 0L, 20L);

	}

	public void disable() {

		if(isEnabled())
			task.cancel();

	}

	@Override
	public boolean isEnabled() {
		return task != null && !task.isCancelled();
	}

	@Override
	public void onPhaseChange(@NotNull Phase phase) {

		switch(phase) {

			case READY -> enable();

			case STOPPED -> disable();

		}

	}

	public void compute() {

		synchronized(printers) {

			for(ScoreboardPrinter printer : printers)
				printer.update(providers);

		}

	}

	public void track(ScoreboardPrinter printer) {
		printers.add(printer);

	}

	public void untrack(ScoreboardPrinter printer) {
		printers.remove(printer);
	}

	public void untrackAll() {
		printers.clear();
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
