package dev.combatlab.client.config;

public final class HudModuleConfig {
  boolean enabled;
  double normalizedX;
  double normalizedY;
  double scale = HudModuleSettings.DEFAULT_SCALE;
  String layout;
  String attachedTo;
  String attachmentSide;
  int attachmentOffset;
}
