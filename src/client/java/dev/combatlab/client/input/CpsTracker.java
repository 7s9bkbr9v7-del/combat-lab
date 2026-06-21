package dev.combatlab.client.input;

import java.util.ArrayDeque;

public final class CpsTracker {
	private static final long WINDOW_NANOS = 1_000_000_000L;
	private final ArrayDeque<Long> clicks = new ArrayDeque<>();

	public void recordClicks(int count, long nowNanos) {
		prune(nowNanos);
		for (int index = 0; index < count; index++) {
			clicks.addLast(nowNanos);
		}
	}

	public int currentCps(long nowNanos) {
		prune(nowNanos);
		return clicks.size();
	}

	private void prune(long nowNanos) {
		long cutoff = nowNanos - WINDOW_NANOS;
		while (!clicks.isEmpty() && clicks.getFirst() <= cutoff) {
			clicks.removeFirst();
		}
	}
}
