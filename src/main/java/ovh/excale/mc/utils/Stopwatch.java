package ovh.excale.mc.utils;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import ovh.excale.mc.UHC;

public class Stopwatch {

	private BukkitTask task;
	private int seconds;
	private int lap;

	public Stopwatch() {
		seconds = 0;
		lap = 0;
		task = null;
	}

	public void start() {
		task = Bukkit.getScheduler()
				.runTaskTimerAsynchronously(UHC.plugin(), this::tick, 20, 20);
	}

	public void stop() {
		if(task != null)
			task.cancel();
	}

	public boolean isRunning() {
		return task != null && !task.isCancelled();
	}

	private void tick() {
		seconds++;
	}

	public int getTotalSeconds() {
		return seconds;
	}

	public int getSeconds() {
		return seconds % 60;
	}

	public int getMinutes() {
		return seconds / 60;
	}

	public void lap() {
		lap = seconds;
	}

	public int getLapDelta() {
		return seconds - lap;
	}

}