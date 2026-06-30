package dev.combatlab.client.hud;

import java.util.Objects;

final class HudLayoutTransition<T> {
  private static final long DEFAULT_DURATION_NANOS = 220_000_000L;

  private final long durationNanos;
  private T target;
  private long startedAtNanos;
  private boolean initialized;

  HudLayoutTransition() {
    this(DEFAULT_DURATION_NANOS);
  }

  HudLayoutTransition(long durationNanos) {
    this.durationNanos = durationNanos;
  }

  Update update(T target) {
    long nowNanos = System.nanoTime();
    if (!initialized) {
      initialized = true;
      this.target = target;
      startedAtNanos = nowNanos - durationNanos;
      return new Update(true, false, 1.0D, true);
    }

    if (!Objects.equals(this.target, target)) {
      this.target = target;
      startedAtNanos = nowNanos;
      return new Update(false, true, 0.0D, false);
    }

    double linearProgress =
        Math.clamp((double) (nowNanos - startedAtNanos) / durationNanos, 0.0D, 1.0D);
    return new Update(false, false, settleProgress(linearProgress), linearProgress >= 1.0D);
  }

  void snapTo(T target) {
    this.target = target;
    startedAtNanos = System.nanoTime() - durationNanos;
    initialized = true;
  }

  void reset() {
    target = null;
    initialized = false;
  }

  static double lerp(double start, double end, double progress) {
    return start + (end - start) * progress;
  }

  private static double settleProgress(double progress) {
    return progress * progress * progress * (progress * (progress * 6.0D - 15.0D) + 10.0D);
  }

  record Update(boolean firstUpdate, boolean targetChanged, double progress, boolean complete) {}
}
