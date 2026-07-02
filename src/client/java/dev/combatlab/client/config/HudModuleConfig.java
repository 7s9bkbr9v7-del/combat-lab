package dev.combatlab.client.config;

public final class HudModuleConfig {
  boolean enabled;
  double normalizedX;
  double normalizedY;
  double scale = HudModuleSettings.DEFAULT_SCALE;
  int textColor = HudModuleSettings.DEFAULT_TEXT_COLOR;
  String layout;
  String attachedTo;
  String attachmentSide;
  int attachmentOffset;
}
