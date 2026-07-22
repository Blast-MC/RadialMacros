package tech.blastmc.radial.util;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ClientTickScheduler {

	private static final List<ScheduledTask> TASKS = new ArrayList<>();
	private static final Queue<ScheduledTask> PENDING_TASKS = new ConcurrentLinkedQueue<>();

	private static boolean initialized;

	public static void init() {
		if (initialized)
			return;

		initialized = true;

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			Iterator<ScheduledTask> iterator = TASKS.iterator();

			while (iterator.hasNext()) {
				ScheduledTask task = iterator.next();

				if (--task.ticksRemaining <= 0) {
					iterator.remove();
					task.action.run();
				}
			}

			ScheduledTask task;
			while ((task = PENDING_TASKS.poll()) != null)
				TASKS.add(task);
		});
	}

	public static void schedule(int ticks, Runnable action) {
		if (ticks < 1)
			throw new IllegalArgumentException("Delay must be at least 1 tick");

		PENDING_TASKS.add(new ScheduledTask(ticks, action));
	}

	private static class ScheduledTask {
		private int ticksRemaining;
		private final Runnable action;

		private ScheduledTask(int ticksRemaining, Runnable action) {
			this.ticksRemaining = ticksRemaining;
			this.action = action;
		}
	}

	private ClientTickScheduler() {}
}