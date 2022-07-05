package ovh.excale.xkuhc.core;

public enum GamePhase {

	READY(false),
	STARTING(true),
	RUNNING(true),
	LETHAL(true),
	ENDING(true),
	STOPPED(false);

	private final boolean running;

	GamePhase(boolean running) {
		this.running = running;
	}

	public boolean isRunning() {
		return running;
	}

}
