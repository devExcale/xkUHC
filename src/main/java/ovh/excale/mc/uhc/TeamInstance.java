package ovh.excale.mc.uhc;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class TeamInstance {

	private final Team team;
	private final String name;
	private final Set<Challenger> alive;
	private final Set<Challenger> dead;

	public TeamInstance(Team team) {
		this.team = team;
		name = team.getName();
		// TODO: ALIVE/DEAD SWITCH ON DEATH
		alive = Collections.synchronizedSet(new HashSet<>());
		dead = Collections.synchronizedSet(new HashSet<>());

		alive.addAll(team.challengers());
	}

	public void die(Challenger challenger) {
		if(alive.remove(challenger)) {
			dead.add(challenger);
			challenger.die();
		}
	}

	public String getName() {
		return name;
	}

	public Set<Challenger> getAlive() {
		return alive;
	}

	public Set<Challenger> getDead() {
		return dead;
	}

	public boolean isEliminated() {
		return alive.size() == 0;
	}

	public boolean isAlive() {
		return alive.size() != 0;
	}

	public void forEachAlive(Consumer<Player> consumer) {
		for(Challenger challenger : alive)
			consumer.accept(challenger.vanilla());
	}

	public void broadcast(String message) {
		for(Challenger challenger : alive)
			challenger.vanilla()
					.sendMessage(message);
		for(Challenger challenger : dead)
			challenger.vanilla()
					.sendMessage(message);
	}

}
