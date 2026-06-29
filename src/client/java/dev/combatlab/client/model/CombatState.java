package dev.combatlab.client.model;

import java.util.UUID;

/** Stable, render-agnostic state consumed by HUD, replay, and coaching features. */
public final class CombatState {
  private float attackStrength;
  private int ping = -1;
  private String targetName;
  private UUID targetId;
  private float targetDistance;

  public void clear() {
    attackStrength = 0.0F;
    ping = -1;
    clearTarget();
  }

  public void clearTarget() {
    targetName = null;
    targetId = null;
    targetDistance = 0.0F;
  }

  public void setTarget(UUID targetId, String targetName, float targetDistance) {
    this.targetId = targetId;
    this.targetName = targetName;
    this.targetDistance = targetDistance;
  }

  public float attackStrength() {
    return attackStrength;
  }

  public void setAttackStrength(float attackStrength) {
    this.attackStrength = attackStrength;
  }

  public int ping() {
    return ping;
  }

  public void setPing(int ping) {
    this.ping = ping;
  }

  public String targetName() {
    return targetName;
  }

  public UUID targetId() {
    return targetId;
  }

  public float targetDistance() {
    return targetDistance;
  }
}
