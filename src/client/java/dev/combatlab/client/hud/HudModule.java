package dev.combatlab.client.hud;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * A self-contained HUD module that can be registered, configured, and moved without teaching the
 * editor about its concrete type.
 */
public interface HudModule {
  Identifier id();

  Component displayName();

  boolean enabled();

  void setEnabled(boolean enabled);

  HudPosition position(int screenWidth, int screenHeight);

  HudSize size();

  HudRectangle bounds(int screenWidth, int screenHeight);

  default HudRectangle editorBounds(int screenWidth, int screenHeight) {
    return bounds(screenWidth, screenHeight);
  }

  void updatePosition(int x, int y, int screenWidth, int screenHeight);

  void savePosition();

  String attachmentTargetId();

  void attachTo(HudModule target, HudAttachmentSide side, int offset);

  void clearAttachment();

  void detach(int screenWidth, int screenHeight);

  void renderInGame(GuiGraphicsExtractor graphics, HudRenderContext context);

  void renderEditorPreview(
      GuiGraphicsExtractor graphics,
      Font font,
      HudRectangle bounds,
      int screenWidth,
      int screenHeight,
      HudGameState gameState);

  default void tick(HudGameState gameState) {}

  default boolean ticksWhenDisabled() {
    return false;
  }

  default boolean contains(double mouseX, double mouseY, int screenWidth, int screenHeight) {
    return bounds(screenWidth, screenHeight).contains(mouseX, mouseY);
  }
}
