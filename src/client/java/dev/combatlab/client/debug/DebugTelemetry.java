package dev.combatlab.client.debug;

import dev.combatlab.client.event.AttackRecordedEvent;
import dev.combatlab.client.event.TargetChangedEvent;

/** Emits state transitions only. Rendering and tick loops deliberately stay quiet. */
public final class DebugTelemetry {
  public void onTargetChanged(TargetChangedEvent event, boolean enabled, DebugLogger debug) {
    if (!enabled) {
      return;
    }
    if (!event.hasTarget()) {
      debug.info("Crosshair target cleared (was {})", event.previousTargetName());
    } else {
      debug.info(
          "Crosshair target: {} at {} blocks",
          event.targetName(),
          String.format("%.2f", event.targetDistance()));
    }
  }

  public void onAttackRecorded(AttackRecordedEvent event, boolean enabled, DebugLogger debug) {
    if (!enabled) {
      return;
    }
    var attack = event.attack();
    debug.info(
        "Attack #{}: target={}, distance={}, strength={}%, ping={}ms, tick={}",
        attack.sequence(),
        attack.hasTarget() ? attack.targetName() : "miss",
        attack.hasTarget() ? String.format("%.2f", attack.targetDistance()) : "n/a",
        Math.round(attack.attackStrength() * 100.0F),
        attack.ping(),
        attack.gameTick());
  }
}
